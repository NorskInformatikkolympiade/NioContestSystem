# The input should be one line for each contestant, formatted as:
# firstName,lastName,username
# Usernames that contain an @ will become participants; others will become admins

from sys import stdin
import random
import string
import sha

def randomString(length):
    return "".join(random.choice(string.ascii_uppercase + string.ascii_lowercase) for x in xrange(length))

passwords = []
for line in stdin:
    items = [x.strip() for x in line.split(",")]
    firstName = items[0]
    lastName = items[1]
    username = items[2]
    isAdmin = str("@" not in username).lower()
    password = randomString(8)
    salt = randomString(8)
    hash = sha.new(password + salt).hexdigest()
    passwords.append((username, password))
    print "Contestant(%s):\n    username: %s\n    firstName: %s\n    lastName: %s\n    isAdmin: %s\n    passwordSalt: %s\n    passwordHash: %s\n" % (username, username, firstName, lastName, isAdmin, salt, hash)
    
for p in passwords:
    print p[0], p[1]
