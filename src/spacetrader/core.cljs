(ns spacetrader.core
    (:require [reagent.core :as r]
	          [spacetrader.components :refer [render-game]]))

(enable-console-print!)

(def container (.getElementById js/document "app"))


;; sample game for rendering
(def game
  {:resources [
        {:type :black :owner :bank}
        {:type :black :owner :bank}
        {:type :black :owner :bank}
        {:type :black :owner :bank}
        {:type :white :owner :bank}
        {:type :white :owner :bank}
        {:type :white :owner :bank}
        {:type :white :owner :bank}
        {:type :red :owner :bank}
        {:type :red :owner :bank}
        {:type :red :owner :bank}
        {:type :red :owner :bank}
        {:type :blue :owner :bank}
        {:type :blue :owner :bank}
        {:type :blue :owner :bank}
        {:type :blue :owner :bank}
        {:type :green :owner :bank}
        {:type :green :owner :bank}
        {:type :green :owner :bank}
        {:type :green :owner :bank}
        {:type :uni :owner :bank}
        {:type :uni :owner :bank}
        {:type :uni :owner :bank}
        {:type :uni :owner :bank}
        {:type :uni :owner :bank}
        ]
   :cards [
        {:id 1 :deck :1 :type :black :cost {:white 1 :red 1 :blue 1 :green 1 } :position 1}
        {:id 19 :deck :1 :type :blue :cost {:black 2 :white 1 } :position 2}
        {:id 37 :deck :1 :type :green :cost {:white 2 :blue 1 } :position 3}
        {:id 55 :deck :1 :type :red :cost {:white 3 } :position 4}
        {:id 2 :deck :1 :type :black :cost {:red 1 :green 2 }}
        {:id 22 :deck :1 :type :blue :cost {:red 1 :blue 1 :green 3 }}
        {:id 23 :deck :1 :type :blue :cost {:black 3 }}
        {:id 62 :deck :1 :type :red :cost {:white 4 } :vp 1}
        {:id 3 :deck :1 :type :black :cost {:white 2 :green 2 }}
        {:id 4 :deck :1 :type :black :cost {:black 1 :red 3 :green 1 }}
        {:id 5 :deck :1 :type :black :cost {:green 3 }}
        {:id 6 :deck :1 :type :black :cost {:white 1 :red 1 :blue 2 :green 1 }}
        {:id 7 :deck :1 :type :black :cost {:white 2 :red 1 :blue 2 }}
        {:id 8 :deck :1 :type :black :cost {:blue 4 } :vp 1}
        {:id 20 :deck :1 :type :blue :cost {:black 1 :white 1 :red 2 :green 1 }}
        {:id 21 :deck :1 :type :blue :cost {:black 1 :white 1 :red 1 :green 1 }}
        {:id 24 :deck :1 :type :blue :cost {:white 1 :red 2 :green 2 }}
        {:id 25 :deck :1 :type :blue :cost {:black 2 :green 2 }}
        {:id 26 :deck :1 :type :blue :cost {:red 4 } :vp 1}
        {:id 38 :deck :1 :type :green :cost {:red 2 :blue 2 }}
        {:id 39 :deck :1 :type :green :cost {:white 1 :blue 3 :green 1 }}
        {:id 40 :deck :1 :type :green :cost {:black 1 :white 1 :red 1 :blue 1 }}
        {:id 41 :deck :1 :type :green :cost {:black 2 :white 1 :red 1 :blue 1 }}
        {:id 42 :deck :1 :type :green :cost {:black 2 :red 2 :blue 1 }}
        {:id 43 :deck :1 :type :green :cost {:red 3 }}
        {:id 44 :deck :1 :type :green :cost {:black 4 } :vp 1}
        {:id 56 :deck :1 :type :red :cost {:black 3 :white 1 :red 1 }}
        {:id 57 :deck :1 :type :red :cost {:blue 2 :green 1 }}
        {:id 58 :deck :1 :type :red :cost {:black 2 :white 2 :green 1 }}
        {:id 59 :deck :1 :type :red :cost {:black 1 :white 2 :blue 1 :green 1 }}
        {:id 60 :deck :1 :type :red :cost {:black 1 :white 1 :blue 1 :green 1 }}
        {:id 61 :deck :1 :type :red :cost {:white 2 :red 2 }}
        {:id 73 :deck :1 :type :white :cost {:black 1 :blue 2 :green 2 }}
        {:id 74 :deck :1 :type :white :cost {:black 1 :red 2 }}
        {:id 75 :deck :1 :type :white :cost {:black 1 :red 1 :blue 1 :green 1 }}
        {:id 76 :deck :1 :type :white :cost {:blue 3 }}
        {:id 77 :deck :1 :type :white :cost {:blue 2 :green 2 }}
        {:id 78 :deck :1 :type :white :cost {:black 1 :red 1 :blue 1 :green 2 }}
        {:id 79 :deck :1 :type :white :cost {:black 1 :white 3 :blue 1 }}
        {:id 80 :deck :1 :type :white :cost {:green 4 } :vp 1}
        {:id 10 :deck :2 :type :black :cost {:black 2 :white 3 :green 3 } :vp 1 :position 1}
        {:id 29 :deck :2 :type :blue :cost {:white 5 :blue 3 } :vp 2 :position 2}
        {:id 47 :deck :2 :type :green :cost {:black 1 :white 4 :blue 2 } :vp 2 :position 3}
        {:id 84 :deck :2 :type :white :cost {:red 5 } :vp 2 :position 4}
        {:id 9 :deck :2 :type :black :cost {:white 3 :blue 2 :green 2 } :vp 1}
        {:id 11 :deck :2 :type :black :cost {:red 2 :blue 1 :green 4 } :vp 2}
        {:id 12 :deck :2 :type :black :cost {:white 5 } :vp 2}
        {:id 13 :deck :2 :type :black :cost {:red 3 :green 5 } :vp 2}
        {:id 14 :deck :2 :type :black :cost {:black 6 } :vp 3}
        {:id 27 :deck :2 :type :blue :cost {:red 3 :blue 2 :green 2 } :vp 1}
        {:id 28 :deck :2 :type :blue :cost {:black 3 :blue 2 :green 3 } :vp 1}
        {:id 30 :deck :2 :type :blue :cost {:blue 5 } :vp 2}
        {:id 31 :deck :2 :type :blue :cost {:black 4 :white 2 :red 1 } :vp 2}
        {:id 32 :deck :2 :type :blue :cost {:blue 6 } :vp 3}
        {:id 45 :deck :2 :type :green :cost {:white 3 :red 3 :green 2 } :vp 1}
        {:id 46 :deck :2 :type :green :cost {:black 2 :white 2 :blue 3 } :vp 1}
        {:id 48 :deck :2 :type :green :cost {:green 5 } :vp 2}
        {:id 49 :deck :2 :type :green :cost {:blue 5 :green 3 } :vp 2}
        {:id 50 :deck :2 :type :green :cost {:green 6 } :vp 3}
        {:id 63 :deck :2 :type :red :cost {:black 3 :red 2 :blue 3 } :vp 1}
        {:id 64 :deck :2 :type :red :cost {:black 3 :white 2 :red 2 } :vp 1}
        {:id 65 :deck :2 :type :red :cost {:white 1 :blue 4 :green 2 } :vp 2}
        {:id 66 :deck :2 :type :red :cost {:black 5 :white 3 } :vp 2}
        {:id 67 :deck :2 :type :red :cost {:black 5 } :vp 2}
        {:id 68 :deck :2 :type :red :cost {:red 6 } :vp 3}
        {:id 81 :deck :2 :type :white :cost {:black 2 :red 2 :green 3 } :vp 1}
        {:id 82 :deck :2 :type :white :cost {:white 2 :red 3 :blue 3 } :vp 1}
        {:id 83 :deck :2 :type :white :cost {:black 2 :red 4 :green 1 } :vp 2}
        {:id 85 :deck :2 :type :white :cost {:black 3 :red 5 } :vp 2}
        {:id 86 :deck :2 :type :white :cost {:white 6 } :vp 3}
        {:id 18 :deck :3 :type :black :cost {:black 3 :red 7 } :vp 5 :position 1}
        {:id 71 :deck :3 :type :red :cost {:red 3 :blue 3 :green 6 } :vp 4 :position 2}
        {:id 72 :deck :3 :type :red :cost {:red 3 :green 7 } :vp 5 :position 3}
        {:id 87 :deck :3 :type :white :cost {:black 3 :red 5 :blue 3 :green 3 } :vp 3 :position 4}
        {:id 15 :deck :3 :type :black :cost {:white 3 :red 3 :blue 3 :green 5 } :vp 3}
        {:id 16 :deck :3 :type :black :cost {:red 7 } :vp 4}
        {:id 17 :deck :3 :type :black :cost {:black 3 :red 6 :green 3 } :vp 4}
        {:id 33 :deck :3 :type :blue :cost {:black 5 :white 3 :red 3 :green 3 } :vp 3}
        {:id 34 :deck :3 :type :blue :cost {:white 7 } :vp 4}
        {:id 35 :deck :3 :type :blue :cost {:black 3 :white 6 :blue 3 } :vp 4}
        {:id 36 :deck :3 :type :blue :cost {:white 7 :blue 3 } :vp 5}
        {:id 51 :deck :3 :type :green :cost {:black 3 :white 5 :red 3 :blue 3 } :vp 3}
        {:id 52 :deck :3 :type :green :cost {:white 3 :blue 6 :green 3 } :vp 4}
        {:id 53 :deck :3 :type :green :cost {:blue 7 } :vp 4}
        {:id 54 :deck :3 :type :green :cost {:blue 7 :green 3 } :vp 5}
        {:id 69 :deck :3 :type :red :cost {:black 3 :white 3 :blue 5 :green 3 } :vp 3}
        {:id 70 :deck :3 :type :red :cost {:green 7 } :vp 4}
        {:id 88 :deck :3 :type :white :cost {:black 7 } :vp 4}
        {:id 89 :deck :3 :type :white :cost {:black 6 :white 3 :red 3 } :vp 4}
        {:id 90 :deck :3 :type :white :cost {:black 7 :white 3 } :vp 5}
        ]  
    :players [{:name "Dan" :vp 1}]
	  :active-player 0
  })
  
(render-game game container)