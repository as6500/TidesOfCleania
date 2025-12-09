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


app.get('/hello', (req, res) => {
    res.send('Hello, World!');
});



app.get("/getGameState", (req, res) => {
        connection.query("SELECT session_id, pairing_code, boost_duration \
            FROM tidesofcleania TOC "
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

                    req.session.sessionId = rows[0].session_id
                    req.session.pairingCode = rows[0].pairing_code
                    req.session.boostDuration = rows[0].boost_duration
                    
                    res.status(200).json({
                        "session_id":  req.session.sessionId,
                        "pairing_code":  req.session.pairingCode,
                        "boost_duration":  req.session.boostDuration
                    })
                }
            }
        )
    
})

app.put("/postGameState", (req, res) => {

        connection.query("UPDATE tidesofcleania SET boost_duration = ? WHERE pairing_code = ?", [req.session.boostDuration], [req.session.pairingCode],
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
app.listen(4000, () => {
    console.log("🙌 Server is running on port 4000. Check http://localhost:4000/")
})