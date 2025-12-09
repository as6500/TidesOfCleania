const mysql = require("mysql2")

var hostname = "33w-4i.h.filess.io";
var database = "Tides of Rubbish_completely";
var port = "61002";
var username = "Tides of Rubbish_completely";
var password = "63d92e7e0e184c6f263b8ec9534e88ef957df9da";

//mysql://Tides of Rubbish_completely:63d92e7e0e184c6f263b8ec9534e88ef957df9da@33w-4i.h.filess.io:61002/Tides of Rubbish_completely

const connectionOptions = mysql.createConnection({
    host: hostname,
    user: username,
    password,
    database,
    port,
});

module.exports = connectionOptions;
