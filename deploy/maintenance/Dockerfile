FROM m.daocloud.io/docker.io/library/docker:27.5.1-cli AS dockercli
FROM python:3.13.11-slim-bookworm
RUN apt-get update && apt-get install -y --no-install-recommends openssl ca-certificates \
    && rm -rf /var/lib/apt/lists/*
COPY --from=dockercli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=dockercli /usr/local/libexec/docker/cli-plugins/docker-compose /usr/local/libexec/docker/cli-plugins/docker-compose
WORKDIR /app
COPY deploy/maintenance/ /app/
COPY deploy/scripts/migrations.sh /app/migrations.sh
COPY release.json /app/release.json
COPY legal/ /app/legal/
ARG BUILD_VERSION=0.1.0
LABEL org.opencontainers.image.version=${BUILD_VERSION}
ARG BUILD_REVISION=workspace
ARG BUILD_GIT_SHA=archive
LABEL io.caratcraft.source-digest=${BUILD_REVISION} org.opencontainers.image.revision=${BUILD_GIT_SHA}
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
CMD ["python", "/app/server.py"]
