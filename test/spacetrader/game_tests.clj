(ns spacetrader.game-tests
  (:require [expectations :refer :all]
           [spacetrader.game :refer :all]))

;; New Game Setup
;; Resources / players
(expect {:green 4 :red 4 :blue 4 :black 4 :white 4 :uni 5}
  (->> (new-game 2) :resources (map :type) frequencies))
(expect {:green 5 :red 5 :blue 5 :black 5 :white 5 :uni 5}
  (->> (new-game 3) :resources (map :type) frequencies))
(expect {:green 7 :red 7 :blue 7 :black 7 :white 7 :uni 5}
  (->> (new-game 4) :resources (map :type) frequencies))
;; all belong to the back
(expect {:bank 25}
  (->> (new-game 2) :resources (map :owner) frequencies))