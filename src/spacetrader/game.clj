(ns spacetrader.game)

;; Functional 'Splendor' - in spaaace

;;deck1
;;deck2
;;deck3
;; Resources - #Player Dependent

;;(def patrons
;; [{:name :resources :vp}])
 
(defn ->resource [type] {:type type :owner :bank})

(def universal-resource (map ->resource (repeat 5 "U")))

(defn- resource-count [p] (if (= p 2) 4 (- (* p 2) 1)))
(defn starting-resources [players]
  (let [n (resource-count players)]
    (concat 
      (map ->resource (repeat n :green))
      (map ->resource (repeat n :red))
      (map ->resource (repeat n :blue))
      (map ->resource (repeat n :black))
      (map ->resource (repeat n :white))
      (map ->resource (repeat 5 :uni)))))

(defn new-game [players]
  {:resources (starting-resources players)
   :patrons []
	:decks []
	:active-player nil})

;;game 
;;:players []
;;:decks []
;;:resources ;; linked to #players
;;:patrons ;; linked to #players}