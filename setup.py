#!/usr/bin/env python
# encoding: utf-8


from distutils.core import setup, Extension


setup (name = 'graphbrain',
       url = 'http://graphbrain.com',
       packages = ['gb'],
       package_dir = {'gb': 'gb'},
       ext_package = 'gb',
       )
