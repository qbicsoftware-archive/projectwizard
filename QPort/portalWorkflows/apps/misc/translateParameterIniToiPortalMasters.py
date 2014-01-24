#!/usr/bin/env python
__author__ = 'wojnar'
from applicake.coreutils.arguments import Argument


class translateParameterIniToiPortalMasters(BasicApp):
    args = [Argument('INTERACTIVE', help='Give input manually for every parameter')]
    parameters = ['label', 'desc', 'value.default', 'section', 'orderindex', 'visibility', 'typus', 'value.possible']

    def execute_run(self):
        arguments = sorted(self.info)
        portalMasters = {}

        for argu in arguments:
            arguAll = argu.split('.')
            if len (arguAll) != 2:
                continue
            argu1 = arguAll[0]
            argu2 = arguAll[1]
            key = argu
            value = arguments[argu]
