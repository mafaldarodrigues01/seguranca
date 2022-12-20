
const router = require('express').Router()
const jwt = require('jsonwebtoken')
const axios = require('axios').default
const { newEnforcer } = require('casbin');
const crypto = require('crypto')
const service = require('./server-service')




const CLIENT_ID = process.env.CLIENT_ID
const CLIENT_SECRET = process.env.CLIENT_SECRET
const CALLBACK = 'callback'




router.get('/', getHome)
router.get('/' + CALLBACK, getCallback)
router.get('/login', getHome)
router.get('/google-login', getGoogleLogin)
router.post('/logout', postLogout)

router.use('/tasks-lists', checkUserLoggedIn)
router.use('/tasks-lists', checkRole)
router.get('/tasks-lists', getTasksLists)
router.get('/tasks-lists/:listId/tasks', getListTasks)
router.post('/tasks-lists/:listId/tasks', postTask)
router.get('/tasks-lists/:listId/tasks/:id', getTask)
router.delete('/tasks-lists/:listId/tasks/:id', deleteTask)



function deleteTask(req, resp, next) {
    return axios.delete
    (
        `https://tasks.googleapis.com/tasks/v1/lists/${req.params.listId}/tasks/${req.params.id}`,
        {
            params:{

            },
            headers: {
                'Authorization': `Bearer ${req.user.access_token}`
            }
        }
    )
        .then(() => resp.redirect(`/tasks-lists/${req.params.listId}/tasks`))
        .catch(next)
}


function checkUserLoggedIn(req, resp, next) {
    if (!req.user) {
        return resp.render('errors',{'error':'LogIn'})
    }
    next()
}

async function checkRole(req, resp,next) {
    const enforcer = await newEnforcer('./role-policy/model.conf', './role-policy/policy.csv')

    const sub = req.user.email // the user that wants to access a resource.
    const obj = req.originalUrl // the resource that is going to be accessed.
    const act = req.method // the operation that the user performs on the resource.

    const enforceResult = await enforcer.enforce(sub, obj, act)

    if (!enforceResult) {
        return resp.render('errors',{'error' : 'Permission'})
    }

    next()

}

function getHome(req, resp) {
    if (req.user) {
        return resp.render('home', { 'user': req.user })
    }
    else
        return resp.render('home')
}


function getGoogleLogin(req, resp) {
    const state = crypto.randomUUID()

    req.session.state = state

    return resp
        .redirect(302,
            // authorization endpoint
            'https://accounts.google.com/o/oauth2/v2/auth?'

            // client id
            + `client_id=${CLIENT_ID}&`

            // scope "openid email"
            + 'scope=openid%20email%20https://www.googleapis.com/auth/tasks&'

            // parameter state is used to check if the user's requesting login
            // is the same making the request to the callback URL
            + `state=${state}&`

            // responde_type for "authorization code grant"
            + 'response_type=code&'

            // redirect uri used to register RP
            + `redirect_uri=http://localhost:3001/${CALLBACK}`
        )
}

function postLogout(req, resp, next) {
    return req
        .logout(function (err) {
            if (err) { return next(err); }
            resp.redirect('/');
        })
}

function getCallback(req, resp, next) {
    if (req.query.state !== req.session.state) {
        resp.status(403).send()
        return
    }

    return axios
        .post(
            'https://www.googleapis.com/oauth2/v3/token',
            {
                code: req.query.code,
                client_id: CLIENT_ID,
                client_secret: CLIENT_SECRET,
                redirect_uri: 'http://localhost:3001/' + CALLBACK,
                grant_type: 'authorization_code'
            }
        )
        .then(res => {

            const json_response = res.data;
            const jwt_payload = jwt.decode(json_response.id_token);

            const user = {
                email: jwt_payload.email,
                id_token: json_response.id_token,
                access_token: json_response.access_token
            }

            service.addUser(user)

            req.logIn(user, err => {
                if (err) { return next(err); }
                return resp.redirect('/');
            })

        })
        .catch(next)
}

function getTasksLists(req, resp, next) {
    return axios
        .get(
            'https://tasks.googleapis.com/tasks/v1/users/@me/lists',
            {
                headers: {
                    'Authorization': `Bearer ${req.user.access_token}`
                }
            }
        )
        .then(res => res.data)
        .then(data => data.items)
        .then(items => resp.render('tasksLists', { 'user': req.user, 'items': items }))
        .catch(next)
}

function getListTasks(req, resp, next) {
    return axios
        .get(
            `https://tasks.googleapis.com/tasks/v1/lists/${req.params.listId}/tasks`,
            {
                headers: {
                    'Authorization': `Bearer ${req.user.access_token}`
                }
            }
        )
        .then(res => res.data)
        .then(data => data.items)
        .then(list => resp.render('listTasks', { 'user': req.user, 'listId': req.params.listId, 'list': list }))
        .catch(next)
}



function postTask(req, resp, next) {
    return axios
        .post(
            `https://tasks.googleapis.com/tasks/v1/lists/${req.params.listId}/tasks`,
            {
                title: req.body.title
            },
            {
                headers: {
                    'Authorization': `Bearer ${req.user.access_token}`
                }
            }
        )
        .then(() => resp.redirect(`/tasks-lists/${req.params.listId}/tasks`))
        .catch(next)
}

function getTask(req, resp, next) {
    return axios
        .get(
            `https://tasks.googleapis.com/tasks/v1/lists/${req.params.listId}/tasks/${req.params.id}`,
            {
                headers: {
                    'Authorization': `Bearer ${req.user.access_token}`
                }
            }
        )
        .then(res => res.data)
        .then(task => resp.render('task', { 'user': req.user, 'task': task }))
        .catch(next)
}

module.exports = router