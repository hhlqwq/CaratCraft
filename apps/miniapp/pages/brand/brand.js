const api = require('../../config/api.js');
const {createPagedFeed} = require('../../services/paged-feed.js');

Page(createPagedFeed({url: api.BrandList, listField: 'brandList', responseField: 'records'}));
