/* ********************************************** Requires ********************************************** */

const express = require('express')
const passport = require('passport')

const routes = require('./server-routes')
const service = require('./server-service')

/* ********************************************** App setup ********************************************** */

const PORT = 3001

//Express instance
const app = express()

/**
 * Setup view engine
 */
app.set('view engine', 'hbs')

/* ********************************************** Middlewares ********************************************** */

app.use(express.json())
app.use(express.urlencoded({ extended: true }))
app.use(require('cookie-parser')())
app.use(require('express-session')({ secret: 'strong hash function', resave: true, saveUninitialized: true }))
app.use(passport.initialize())
app.use(passport.session())

passport.serializeUser((user, done) => {
    done(null, user.id_token)
})
passport.deserializeUser((userId, done) => {
    const user = service.getUser(userId)
    done(null, user)
})

app.use('/', routes)


app.listen(PORT, (err) => {
    if (err) {
        return console.log('something bad happened', err)
    }
    console.log(`server is listening on ${PORT}`)
})