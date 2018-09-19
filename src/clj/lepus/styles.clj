(ns lepus.styles
  (:require [garden-watcher.def :refer [defstyles]]))

(defstyles style
  [:h1 {:background-color "red"}]
  [:select
   {:background-color "lightgrey"
    :-moz-appearance "none"
    :-webkit-appearance "none"
    :appearance "none"
    :border "none"}]
  [:input
   {:background-color "lightgrey"
    :border "none"}]
  [:button
   {:background-color "lightgrey"
    :border "none"}]
  [:#wrap
   {:width "1220px"
    :margin "0 auto"}]
  [:#left
   {:width "200px"
    :height "90vh"
    :padding "5px"
    :background-color "grey"
    :float :left}]
  [:#logo
   {:font-size "30px"
    :font-family "monospace"
    :margin-right "30px"
    :margin-left "10px"
    :background-color "orange"}]
  [:#logo1
   {:background-color "black"
    :padding "5px"
    :color "white"}]
  [:#middle
   {:width "600px"
    :height "90vh"
    :padding "5px"
    :overflow-y "scroll"
    :background-color "black"
    :color "white"
    :float :left}])
