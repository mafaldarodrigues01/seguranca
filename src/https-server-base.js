
const https = require("https");
const express = require("express");
var fs = require('fs');

const PORT = 4433;
const app = express();

// Get request for resource /
app.get("/", function (req, res) {
    if (options.requestCert === true) {
        res.send("<html><body>Connection with authentication</body></html>");
    }else {
        res.send("<html><body>Connection without authentication</body></html>");
    }
});

// configure TLS handshake
const options = {
    key: fs.readFileSync('secure-server-pfx.pem'),
    cert: fs.readFileSync('secure-server-cer.pem'), // CA1-int.cer
    ca: fs.readFileSync('Alice_2.cer'),
    requestCert: true,  // false -> no auth, true -> auth
    rejectUnauthorized: false
};

// Create HTTPS server
https.createServer(options, app).listen(PORT,
    function () {
        console.log("Server started at port " + PORT);
    }
);

