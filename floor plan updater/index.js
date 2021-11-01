const fs = require('fs');
const axios = require('axios');
const https = require('https');

function post_request(node) {
    const agent = new https.Agent({  
        rejectUnauthorized: false
    });
    axios.post('https://52.14.13.109/postnodes/', {
        data: node,
        httpsAgent: agent,
    });
}

async function upload_bbb_nodes() {
    let rawdata = fs.readFileSync('BBBInternalNodes.json');
    let bbbnodes = JSON.parse(rawdata);
    for (let i = 0; i < bbbnodes['BBB'].length; i++) {
        bbbnodes['BBB'][i]['building_name'] = 'BBB';
        cur_node = bbbnodes['BBB'][i];
        console.log(cur_node)
        await post_request(cur_node);
    }
}

function main() {
    upload_bbb_nodes();
}

main();