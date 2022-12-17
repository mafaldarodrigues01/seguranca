
const users = []

function addUser(user) {
    users[user.id_token] = user
}

function getUser(userId) {
    return users[userId]
}

module.exports = {
    addUser,
    getUser
}