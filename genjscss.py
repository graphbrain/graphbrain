#!/usr/bin/env python
# -*- coding: utf-8 -*-


import os, os.path, shutil


SCRIPTS = [
    'js/aux.js',
    'js/geom.js',
    'js/vec3mat4x4.js',
    'js/quat.js',
    'js/visualobj.js',
    'js/node.js',
    'js/snode.js',
    'js/link.js',
    'js/graph.js',
    'js/uimodes.js',
    'js/main.js',
    ]

SCRIPTS_OUT_DEBUG = 'static/js/gb.js'
SCRIPTS_OUT = 'static/js/gb.min.js'

STYLESHEETS = [
    'css/main.css',
    'css/interface.css',
    'css/nodes.css',
    'css/links.css',
    'css/dialog.css',
    ]

STYLESHEETS_OUT = 'static/css/main.min.css'


YUI_COMPRESSOR = 'yuicompressor-2.4.6.jar'


def compress(in_files, out_file, in_type='js', verbose=False,
             temp_file='.temp'):
    temp = open(temp_file, 'w')
    for f in in_files:
        fh = open(f)
        data = fh.read() + '\n'
        fh.close()

        temp.write(data)

        print ' + %s' % f
    temp.close()

    options = ['-o "%s"' % out_file,
               '--type %s' % in_type]

    if verbose:
        options.append('-v')

    os.system('java -jar "%s" %s "%s"' % (YUI_COMPRESSOR,
                                          ' '.join(options),
                                          temp_file))

    org_size = os.path.getsize(temp_file)
    new_size = os.path.getsize(out_file)

    print '=> %s' % out_file
    print 'Original: %.2f kB' % (org_size / 1024.0)
    print 'Compressed: %.2f kB' % (new_size / 1024.0)
    print 'Reduction: %.1f%%' % (float(org_size - new_size) / org_size * 100)
    print ''


def main():
    print 'Compressing JavaScript...'
    compress(SCRIPTS, SCRIPTS_OUT, 'js', False, SCRIPTS_OUT_DEBUG)

    print 'Compressing CSS...'
    compress(STYLESHEETS, STYLESHEETS_OUT, 'css')


if __name__ == '__main__':
    main()
