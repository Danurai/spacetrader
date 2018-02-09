(ns spacetrader.components
  (:require [reagent.core :as r]))

 ;; Atoms
(def app-data (r/atom nil))
(def trade-data (r/atom nil))

;; utilities
(defn alert [message]
"Shows alert <message> and returns false"
   (swap! trade-data assoc :alert message)
   false)

(defn- filter-by-owner [owner coll]
   (->> coll (filter #(= owner (:owner %)))))
   
;; Facilities
;; SHOULD FORM PART OF GAME.CLJ?

(defn- next-facility-id [deck]
   (->> (:cards @app-data) 
       (filter #(= deck (:deck %)))
       (filter #(nil? (:owner %)))
       (filter #(nil? (:position %))) 
       first :id))

(defn- facility-idx [id]
   (count (take-while #(not= id (:id %)) (:cards @app-data))))
   
(defn- resource-idx 
   [type zone]
   (count (take-while #(or (not= type (:type %)) (not= zone (:owner %)) (true? (:temp %)) ) (:resources @app-data))))
          
(defn draw-facility [deck position] ;; should be able to work these out
   (let [next-id (next-facility-id deck)]
      (swap! app-data assoc :cards
                          (mapv #(if (= next-id (:id %))
                                    (assoc % :position position)
                                    %) (:cards @app-data)))))

(defn assign-facility [fac-id uid]   
   (swap! app-data assoc :cards
                       (mapv #(if (= fac-id (:id %))
                                 (-> % (assoc :owner uid)
                                      (dissoc :position)
                                      (dissoc :reserved))
                                  %) (:cards @app-data) )))

;; Temp Zone 
(defn- return-temp-resources []
   (swap! app-data assoc :resources (mapv #(dissoc % :temp) (:resources @app-data)))
   (reset! trade-data nil))
                       
(defn- return-temp-facilites []
   (swap! app-data assoc :cards (mapv #(dissoc % :temp) (:cards @app-data)))
   (reset! trade-data nil))
                       
(defn- take-resources   []
   ;; Check if fewer than 2/3 resources
   (swap! app-data assoc :resources (mapv #(if (:temp %) 
                                          (-> % (assoc :owner 0) 
                                                (dissoc :temp))
                                           %) (:resources @app-data)))
   (reset! trade-data nil))
                       

;; Reserve Facility  
(defn- canreservefacility? []
   (let [player 0]
      (if (= 3 (->> @app-data :cards (filter-by-owner player) (filter :reserved) count))
         (alert "You can only reserve 3 facilities")
         (if (= 0 (->> @app-data :cards (filter :temp) count))
            (alert "No facility selected")
            true))))
         
(defn- reserve-facility []
   (let [facility (->> @app-data :cards (filter :temp) first)]
      (when (canreservefacility?)
      ;; ISSUE CALL TO GAME.CLJ - TODO FIX THIS
         ;; reserve facility 
         (swap! app-data update-in [:cards (facility-idx (:id facility))] assoc :owner 0 :reserved true)
         (swap! app-data update-in [:cards (facility-idx (:id facility))] dissoc :temp :position)
         ;; take one :uni
         (if (< 0 (->> @app-data :resources (filter-by-owner :bank) (map :type) frequencies :uni))
            (swap! app-data update-in [:resources (resource-idx :uni :bank)] assoc :owner 0))
         ;; draw one
         (draw-facility (:deck facility) (:position facility))
         ;; clean-up
         (return-temp-resources)
      )))

;; Buy Facility
(defn- canbuyfacility? [facility]
   (let [facility-owned (->> @app-data :cards (filter-by-owner 0) (filter #(nil? (:reserved %))) (map :type))
        resource-assigned (->> @app-data :resources (filter-by-owner 0) (filter :temp) (map :type))
        balance (reduce #(update %1 %2 dec) (:cost facility)  (concat facility-owned resource-assigned))]
      ;; TODO Apply Temp Resource  
      (if (< 0 (- (apply max (vals balance)) (-> resource-assigned frequencies (:uni 0))))
         (alert (map #(str (val %) " more " (name (key %)) "\n") balance))
         true)))
   
(defn- buy-facility  []
   (let [facility (->> @app-data :cards (filter :temp) first)]
;; Buy the temp facility
      (when (canbuyfacility? facility)  ;; checks if enough resources are available \ committed
      ;; ISSUE CALL TO GAME.CLJ
         ;; assign temp resources to Bank
         (swap! app-data assoc :resources (mapv #(if (:temp %) (assoc % :owner :bank) %) (:resources @app-data)))
         ;; assign facility to player
         (assign-facility (:id facility) 0) 
         ;; draw new facility  
         (draw-facility (:deck facility) (:position facility))
         ;; clean up
         (return-temp-resources)
         (return-temp-facilites)
      )))   
  
   
(defn facility-click
"Handle Clicking on a facility"
   [fac-id uid]
   (return-temp-resources)
   (return-temp-facilites)
   (swap! app-data assoc-in [:cards (facility-idx fac-id) :temp] true)
   (swap! trade-data assoc :title "Trade Zone - Purchasing"
                        :buttons [{:value "Cancel" :click return-temp-facilites}
                                 {:value "Buy" :click buy-facility}
                                 {:value "Reserve" :click reserve-facility}]))
   
;; Render Facilities
(defn render-small-resource 
  ([card-freqs key]
      ^{:key (name key)}
      [:div {:class (str "res-small res-" (name key))}
         [:span {:class "res-count"} (key card-freqs 0)]]))
         
(defn render-facility [fac]
  ^{:key (:id fac)}
    [:div {:class (str "card card-back-" (name (:deck fac)))
          :on-click #(facility-click (:id fac) 0)}
      [:div {:class (str "card-banner res-" (name (:type fac)))}
        [:div.pull-left (:id fac)]
        [:div.pull-right (if (:vp fac) (str "VP: " (:vp fac)))]]
      [:div 
        [:div.res-cost
          (map #(render-small-resource (:cost fac) %) (keys (:cost fac)))
        ]]])

(defn render-facility-set [id]
   ^{:key id}[:div.row
      [:div.facility
         [:div {:class (str "card card-deck card-back-" (name id))
               :on-click nil} ;; automatically reserve 
            (->> @app-data :cards (filter-by-owner nil) (filter #(= id (:deck %))) (filter #(nil? (:position %))) count)]
         (map #(render-facility %) 
                     (->> (:cards @app-data)
                         (filter #(= (:deck %) id))
                         (filter :position)
                         (sort-by :position)))]])

;; Resources

(defn CanTakeResource? [res]
   "Checks if a resource [res] can be added to the temporary zone, returns true or false. Rules:
   #1 cannot take more than 3 resource
   #2 cannot take more than 2 of the same resource
   #2.1 and leave fewer than 2 of the same resource"
   (let [tmp-resources (->> @app-data :resources (filter :temp) (map :type))
        bank-resources (->> @app-data :resources (filter #(nil? (:temp %))) (filter-by-owner :bank) (map :type))]
      (if (= res :uni)
         (alert "You can only take universal resource when you reserve a facility")
         (if (empty? tmp-resources)                                                                   ;; Have any resources been taken?
            true
            (case (count tmp-resources)
               1  (if (and (= res (first tmp-resources))                                               ;; Are there 1 resources
                          (> 3 (res (frequencies bank-resources))))  ;; > motonically ascending if 3 > x    ;; you're taking a second and there are only 2 left
                          (alert "You need to leave at least 2 of those if you want to take 2.")
                          true)
               2  (if (= 1 (count (distinct tmp-resources)))                                            ;; Are there 2 resources - Are they both the same type?
                     (alert "You can only take 2 resources if they are the same type.")
                     (if (= 1 (res (frequencies tmp-resources)))                                        ;; - Is one of them the same as the one you're taking 
                        (alert "You can only take 3 resources if they are different types.")
                        true))
               3  (alert "No more than 3 resources, spacer")                                             ;; Are there 3 resources
               true)))))
                  
(defn- resource-click [type zone]
   (case zone 
      :bank (when (CanTakeResource? type)
               (return-temp-facilites)
               (swap! app-data assoc-in [:resources (resource-idx type zone) :temp] true)
               (swap! trade-data assoc :title "Trade Zone - Acquiring Resource"
                                    :buttons [{:value "Take" :click take-resources}
                                             {:value "Cancel" :click return-temp-resources}]))
      :temp  (;;(swap! app-data update-in [:resources ] dissoc :temp)
             (if (empty? (->> @app-data :resources (filter :temp)))
                (reset! trade-data nil)))
      (0 1 2 3) ((swap! app-data assoc-in [:resources (resource-idx type zone) :temp] true)
                (if (empty? (->> @app-data :cards (filter :temp)))
                   (swap! trade-data assoc :title "Trade zone - Select a facility"
                                        :buttons [{:value "Cancel" :click return-temp-resources}])))
      :default))
                   
(defn- render-resource [type zone]
   (let [resource-freqs (if (= zone :temp)
                         (->> @app-data :resources (filter :temp) (map :type) frequencies)
                         (->> @app-data :resources
                             (filter-by-owner zone)
                             (filter #(nil? (:temp %)))
                             (map :type) 
                             frequencies))]
   ^{:key type}[:div {:class (str "res res-" (name type) (if-not (type resource-freqs) " res-zero"))
                       :on-click (if (type resource-freqs)     ;;and (type resource-freqs) (not= type :uni)) 
                                    #(resource-click type zone))}
                   [:span.res-count (type resource-freqs 0)]]))
                   
(defn render-bank-resources []
   [:div 
      [:p.sector-name "Bank Zone"]
      [:div.resource-pool  
         (doall (map #(render-resource % :bank) (->> @app-data :resources (map :type) distinct)))]])
                         
(defn render-temp-zone []
  [:div {:class (str "temp-zone" (if (empty? (:title @trade-data)) " temp-zone-hidden"))}
      [:p.sector-name (:title @trade-data)]
      (let [facility (->> @app-data :cards (filter :temp) first)]
         (when-not (empty? facility)
            [:div
               [:div.facility (render-facility facility)]]))
      [:div.resource-pool
         (doall (map #(render-resource % :temp) (->> @app-data :resources (filter :temp) (map :type) distinct)))]
      [:div
         (for [btn (:buttons @trade-data)]
            ^{:key (:value btn)}[:button {:on-click (:click btn)} (:value btn)])]])
            

;; PLAYMAT

(defn render-player-facility [fac-type]
   ^{:key (key fac-type)}[:div.resource-pool {:class (str "user-facility res-" (name (key fac-type)))} (val fac-type)])
            
(defn render-player [player-id]
   [:div 
      [:p.sector-name (str (get-in @app-data [:players player-id :name]) " VP: " 
                    (reduce + (->> @app-data :cards (filter-by-owner 0) (filter #(nil? (:reserved %))) (map :vp))))]
      [:p "Resource"]
      [:div.resource-pool
         (doall (map #(render-resource % player-id) 
                     (->> @app-data :resources (filter-by-owner player-id) (map :type) distinct)))]
      ;;[:p (->> @app-data :resources (filter-by-owner 0))]
      [:div.resource-pool
         [:p.sector-name "Facilities"]
         (map #(render-player-facility %) (->> @app-data :cards (filter-by-owner player-id)
                                                          (filter #(nil? (:reserved %)))
                                                          (map :type) 
                                                          frequencies))
         ]
      [:div.facility
         [:p.sector-name "(reserved)"]
         [:div.resource-pool (map #(render-facility %) (->> @app-data :cards (filter-by-owner 0) (filter :reserved)))]]
      
      [:p "Total available resource"]
      [:p (str (frequencies (concat (->> @app-data :resources (filter-by-owner 0) (map :type))
                                (->> @app-data :cards (filter-by-owner 0) (filter #(nil? (:reserved %))) (map :type))))) ]])
;;   [:div 
;;      [:p.sector-name (get-in @app-data [:players player-id :name])]
;;      [:div.resource-pool
;;         (doall (map #(render-resource % player-id) 
;;                     (->> @app-data :resources (filter #(= (:owner %) player-id)) (map :type) distinct)))]
;;      [:p "Facilities owned"]
;;      [:div.resource-pool
;;         (doall (map #(render-player-facility %) (->> @app-data :cards (filter #(= player-id (:owner %))) (map :col) frequencies) ))]])

;; Page Layout

;;------------------------------------
;;   Facilities L3 - L1     | player |
;;                          | order  |
;;                          | and    |
;;                          | summary|
;;--------------------------|  |     |
;; Trade Zone               | <-     |
;;------------------------------------
;; Player info                       |
;; Resources, Facilities and Patrons |
;;------------------------------------
        
(defn Game [game]
   [:div ;;data.react-root
      [:div.container-fluid
         [:div.row
            [:div.col-sm-9    ;; Board
               [:div#facility
                  [:p.sector-name "Facilities"]
                  (doall (map #(render-facility-set %) [:3 :2 :1]))]  ;; Facilities
               [:div.row (render-bank-resources)]                    ;; Resources
               [:div.row (render-temp-zone)]                         ;; Temporary Area - default source is :bank
               [:div.row (render-player 0)]                          ;; Player Area
               ]
            [:div.col-sm-3
               [:div [:button {:on-click #(reset! app-data game)}]]
               [:div#alert (:alert @trade-data)]                       ;; Alert Box
                                                                  ;; Player info?
            ]]]])
            
;; Setup placeholder
(defn- update-app-data [game]
   (reset! app-data game)
   (swap! app-data assoc :players [{:name "Dan"}]))
  
(defn render-game [game container]
  (update-app-data game)
  (r/render [Game game] container))