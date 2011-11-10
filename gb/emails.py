# -*- coding: utf-8 -*-


from datetime import datetime
import mongodb


def add_email(email, ip_addr):
    db = mongodb.getdb()
    memails = db.emails

    if memails.find_one({'email': email}) is None:
    	memails.insert({'email': email, 'ts': datetime.now(), 'ip_addr': ip_addr})
    	return True
    
    return False