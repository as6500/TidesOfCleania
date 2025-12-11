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
        connection.query("SELECT session_id, pairing_code, boost_duration \
            FROM tidesofcleania\
            WHERE pairing_code = ?", [req.pairingCode]
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

                    req.sessionId = rows[0].session_id
                    req.pairingCode = rows[0].pairing_code
                    req.boostDuration = rows[0].boost_duration
                    
                    res.status(200).json({
                        "session_id":  req.sessionId,
                        "pairing_code":  req.pairingCode,
                        "boost_duration":  req.boostDuration
                    })
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
        connection.query("INSERT INTO tidesofcleania (pairing_code, boost_duration) VALUES (?,?)", [req.pairingCode], [req.boostDuration],
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