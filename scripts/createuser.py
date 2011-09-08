#!/usr/bin/env python


import sys

import user


if __name__ == '__main__':
    email = sys.argv[1] 
    name = sys.argv[2] 
    password = sys.argv[3] 
    role = int(sys.argv[4])
    u = user.User()
    u.create(email, name, password, role)
