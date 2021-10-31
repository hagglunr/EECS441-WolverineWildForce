const fs = require('fs');
const axios = require('axios')

function post_request(node) {
    console.log('here')
    axios.post('https://3.19.66.229/post_nodes/', node);
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