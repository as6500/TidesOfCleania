//Import modules
const express = require("express")
const bodyParser = require("body-parser")
const connection = require("./database")
const session = require("express-session")

//Initialize express
const app = express()

//Apply body parser to the incoming requests
app.use(bodyParser.urlencoded({extended: false}))

// Parses the body of the request as JSON

app.use(express.json())

//Connect to database
connection.connect((err) => {
    if (err) {
        console.log("¯\_(ツ)_/¯ Error connecting to DB : " + err)
        return
    }
    console.log("🦄 Connected to the DB")
} )


app.get("/getGameState", (req, res) => {
    const pairingCode = req.query.pairingCode;
    const params = [pairingCode];

        connection.query(
            "SELECT session_id, pairing_code, boost_duration \
            FROM tidesofcleania\
            WHERE pairing_code = ?", params
            ,
            function (err, rows, fields) {
                if (err) {
                    console.log("Database Error: " + err)
                    res.status(500).json({
                        "message": err
                    })
                    return
                } 

                if (rows.length != 0) {
                const row = rows[0];

                res.status(200).json({
                    "session_id": row.session_id,
                    "pairing_code": row.pairing_code,
                    "boost_duration": row.boost_duration
                });
                } 
                else {
                    res.status(404).json({
                        "message": "Code not found"
                    });
                }
            }
        )
    
})

app.put("/updateGameState", (req, res) => {

        connection.query("UPDATE tidesofcleania SET boost_duration = ? WHERE pairing_code = ?", [req.boostDuration], [req.pairingCode],
            function(err, rows, fields) {
                if (err) {
                    console.log("Database Error: " + err)
                    res.status(500).json({
                        "message": err
                    })
                    return
                }
                
            }
        )
})

app.post("/insertGameState", (req, res) => {
    connection.query("INSERT INTO tidesofcleania (pairing_code, boost_duration) VALUES (?,?)", 
        [req.body.pairing_code, req.body.boost_duration],
        function(err, rows, fields) {
            if (err) {
                console.log("Database Error: " + err)
                res.status(500).json({
                    "message": err
                })
                return
            }
                
        }
    )
})


// listen for requests on port 
const PORT = process.env.PORT || 4000;
app.listen(PORT, () => 
    console.log(`Server running on port ${PORT}`)
);