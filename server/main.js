//Import modules
const express = require("express")
const bodyParser = require("body-parser")
const session = require("express-session")

//Initialize express
const app = express()

//Apply body parser to the incoming requests
app.use(bodyParser.urlencoded({extended: false}))

// Parses the body of the request as JSON

app.use(express.json())

/*
//Connect to database
connection.connect((err) => {
    if (err) {
        console.log("¯\_(ツ)_/¯ Error connecting to DB : " + err)
        return
    }
    console.log("🦄 Connected to the DB")
} )

//Create session cookies
app.use(session({
    secret: "MySuperSecretKey", 
    resave: false,
    saveUninitialized: true,
    cookie: {
        maxAge: 600000000
    }
}))
*/

app.get('/hello', (req, res) => {
    res.send('Hello, World!');
});

// listen for requests on port 
app.listen(80, () => {
    console.log("🙌 Server is running on port 4000. Check http://localhost:4000/")
})