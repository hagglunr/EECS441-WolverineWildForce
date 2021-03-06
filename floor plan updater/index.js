process.env['NODE_TLS_REJECT_UNAUTHORIZED'] = '0';
const fs = require('fs');
const axios = require('axios');
const https = require('https');

function post_request(node) {
    axios
    .post('https://52.14.13.109/postnodes/', node)
    .then(res => {
        console.log(`statusCode: ${res.status}`)
        console.log(res)
    })
    .catch(error => {
        console.error(error)
    })
}

function upload_bbb_nodes() {
    let rawdata = fs.readFileSync('BBBInternalNodes.json');
    let bbbnodes = JSON.parse(rawdata);
    for (let i = 0; i < bbbnodes['Michigan League'].length; i++) {
        bbbnodes['Michigan League'][i]['building_name'] = 'Michigan League';
        cur_node = bbbnodes['Michigan League'][i];
        console.log(cur_node)
        post_request(cur_node);
    }
}

function main() {
    upload_bbb_nodes();
}

main();