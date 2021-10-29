const fs = require('fs');
const axios = require('axios')

function post_request(node) {
    axios.post('https://3.19.66.229/post_nodes/', node)
        .then(console.log('node sent!'));
}

async function upload_bbb_nodes() {
    let rawdata = fs.readFileSync('BBBInternalNodes.json');
    let bbbnodes = JSON.parse(rawdata);
    for (let i = 0; i < bbbnodes['BBB']; i++) {
        bbbnodes['BBB'][i]['building_name'] = 'BBB';
        cur_node = JSON.stringify(bbbnodes['BBB'][i]);
        await post_request(cur_node);
    }
}

function main() {
    upload_bbb_nodes();
}

main();