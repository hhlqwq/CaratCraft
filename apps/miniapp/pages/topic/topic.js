const api = require('../../config/api.js');
const {createPagedFeed} = require('../../services/paged-feed.js');

Page(
    createPagedFeed({
        url: api.TopicList,
        listField: 'topicList',
        responseField: 'records',
        replace: true,
    })
);
