#!/usr/bin/env bash

# 计算单个文件的SHA256, 兼容Linux及macOS自带工具.
sha256_file() {
    local sum
    if command -v sha256sum >/dev/null 2>&1; then
        sum="$(sha256sum -- "$@")" || return $?
    else
        sum="$(shasum -a 256 -- "$@")" || return $?
    fi
    [[ "$sum" =~ ^([0-9a-fA-F]{64})[[:space:]] ]] || return 1
    printf '%s\n' "${BASH_REMATCH[1]}" | tr 'a-f' 'A-F'
}

# 生成来自显式清单的SQL批次, 两端使用相同的结构检查和记录格式.
migration_batch() {
    local registry="$ROOT_DIR/database/upgrade/local" entry sequence directory id ids=""
    local previous=0 checksum check apply stage complete staged
    local completed="${1:-}" mode="${2:-}" pending=""
    local entries=()
    while IFS= read -r entry || [[ -n "$entry" ]]; do
        entry="${entry%$'\r'}"
        [[ "$entry" =~ ^([0-9]{4})\ ([a-z][a-z0-9-]{0,48})$ ]] || {
            echo "本机迁移清单条目无效: $entry" >&2; return 1;
        }
        sequence="${BASH_REMATCH[1]}"
        (( 10#$sequence > previous )) || { echo '本机迁移清单顺序错误。' >&2; return 1; }
        previous=$((10#$sequence))
        id="$sequence-${BASH_REMATCH[2]}"
        ids="${ids:+$ids,}'$id'"
        entries+=("$entry")
    done < "$registry/manifest.txt"
    [[ -n "$ids" ]] || { echo '本机迁移清单不能为空。' >&2; return 1; }
    sed "s/__IDS__/$ids/g" "$registry/begin.sql" || return 1
    for entry in "${entries[@]}"; do
        id="${entry/ /-}"
        directory="$ROOT_DIR/database/upgrade/${entry#* }"
        check="$(sha256_file "$directory/check.sql")" || return 1
        apply="$(sha256_file "$directory/apply.sql")" || return 1
        checksum="$(printf '%s%s' "$check" "$apply" | sha256_file -)" || return 1
        complete=0
        if grep -Fxq "$id" <<< "$completed"; then complete=1; fi
        sed -e "s/__ID__/$id/g" -e "s/__SHA256__/$checksum/g" -e "s/__COMPLETED__/$complete/g" "$registry/before.sql" || return 1
        if [[ "$complete" == 1 ]]; then
            printf "SELECT '%s: verified';\n" "$id"
            continue
        fi
        if [[ "$mode" == preflight ]]; then
            cat "$directory/check.sql" || return 1
            printf '%s\n' 'CREATE TEMPORARY TABLE cc_compatibility_check (ok TINYINT NOT NULL CHECK(ok=1));' \
                'INSERT INTO cc_compatibility_check VALUES(COALESCE(@cc_compatible,0));' \
                'DROP TEMPORARY TABLE cc_compatibility_check;'
            continue
        fi
        if [[ "$mode" == baseline ]]; then
            printf -v staged "INSERT INTO CC_SCHEMA_MIGRATION VALUES ('%s', '%s', CURRENT_TIMESTAMP);\nSELECT '%s: baseline';" "$id" "$checksum" "$id"
            pending+="$staged"$'\n'
            continue
        fi
        staged="$(
            printf '%s\n' 'SET @cc_compatible = NULL, @cc_complete = NULL;'
            for stage in "$directory/check.sql" "$registry/apply.sql" "$directory/apply.sql" \
                "$directory/check.sql" "$registry/after.sql"; do
                sed -e "s/__ID__/$id/g" -e "s/__SHA256__/$checksum/g" "$stage" || exit 1
            done
        )" || return 1
        pending+="$staged"$'\n'
    done
    if [[ "$mode" == preflight ]]; then
        return
    fi
    if [[ "$mode" == baseline ]]; then
        cat "$registry/current-schema.sql" || return 1
        printf 'START TRANSACTION;\n%sCOMMIT;\n' "$pending"
    else
        printf '%s' "$pending"
        cat "$registry/current-schema.sql"
    fi
}

