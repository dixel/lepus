(ns lepus.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [lepus.core-test]
   [lepus.common-test]))

(enable-console-print!)

(doo-tests 'lepus.core-test
           'lepus.common-test)
