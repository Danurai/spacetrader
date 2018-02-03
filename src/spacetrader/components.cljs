(ns spacetrader.components
  (:require [reagent.core :as r]))
;;  Patrons ---
;;  L3 facility
;;  L2 facility  
;;  L1 facility
;;  Resource Pool
;;  Temporary Zone / Take Resources, Spend Resources
;;  Player info

(def app-data (r/atom nil))
     
;; Facilities
(defn render-small-resource 
  ([card-freqs key]
      ^{:key (name key)}
      [:div {:class (str "res res-small res-" (name key))}
         [:span {:class "res-count"} (key card-freqs 0)]]))
         
(defn render-facility [fac]
  ^{:key (:id fac)}
    [:div {:class (str "card card-back-" (name (:deck fac)))
          :on-click (fn [e]
	                   (.preventDefault e)
	                   (println (:id fac)))}
      [:div {:class (str "res-banner res-" (name (:col fac)))}
        [:div {:class "pull-right"} (if (:vp fac) (str "VP: "(:vp fac)))]]
      [:div 
        [:div.res-cost
          (map #(render-small-resource (:cost fac) %) (keys (:cost fac)))
        ]]])

(defn render-facility-set [id]
   ^{:key id}[:div.row
      (map #(render-facility %) 
                  (->> (:cards @app-data)
                      (filter #(= (:deck %) id))
                      (filter :position)
                      (sort-by :position)))])

;; Resources

(defn CanTakeResource? 
"Checks if a resource [res] can be added to the temporary zone"
   [res]
   ;; Rules
   ;; #1 cannot take more than 3 resource
   ;; #2 cannot take more than 2 of the same resource
   ;; #2.1 and leave fewer than 2 of the same resource
  ;; (if (= 2 (->> @app-data :resources (filter #(= (:owner %) :temp)) (map :type) frequencies sort first val) )
  ;;    (do (swap! app-data assoc :alert "You can't take more that 2 of the same resources") false)  ;; No more that 2 of the same, and 2 if you're taking the same color
      (if (and (= 1 (->> @app-data :resources (filter #(= (:owner %) :temp)) (filter #(= (:type %) res)) count))
              (= 2 (->> @app-data :resources (filter #(= (:owner %) :temp)) count)))            ;; have 1 of two and take a third
         (do (swap! app-data assoc :alert "You can only take up to 3 different resources") false)
         (if (= 3 (->> @app-data :resources (filter #(= (:owner %) :temp)) count))
             (do (swap! app-data assoc :alert "You can't take more than 3 resources") false)       ;; No more than 3
             (if (and (= 1 (->> @app-data :resources (filter #(= (:owner %) :temp)) (filter #(= (:type %) res)) count))
                     (= 2 (->> @app-data :resources (filter #(= (:owner %) :bank)) (filter #(= (:type %) res)) count)))
               (do (swap! app-data assoc :alert "You can't take more than one of a resource and leave fewer that two") false) ;; Take 2 and leave fewer than 2
               true))))
;; TODO - have 2 of one.
(defn- resource-idx [owner type]
   (.indexOf (:resources @app-data) {:type type :owner owner}))   

(defn- move-resource [res-key owner src]
   (if (CanTakeResource? res-key)
      (swap! app-data assoc-in [:resources (resource-idx owner res-key) :owner] (if (= owner :temp) src :temp))))
   
(defn- render-resource [res-key owner src]
   (let [resource-freqs (->> @app-data :resources 
                          (filter #(= (:owner %) owner)) 
                          (map :type) 
                          frequencies)]
   ^{:key res-key}[:div {:class (str "res res-" (name res-key) (if-not (res-key resource-freqs) " res-zero"))
                   :on-click (if (res-key resource-freqs) #(move-resource res-key owner src))}
               [:span.res-count (res-key resource-freqs 0)]]))

(defn render-bank-resources []
   [:div 
      [:p.facility-set "Bank Zone"]
      [:div.resource-pool  
         (doall (map #(render-resource % :bank :temp)
                     (->> @app-data :resources (map :type) distinct)))]])

(defn- return-temp-resources
"Return :owner of all resources from :temp to the specified [src]"
   [src]
   (swap! app-data assoc :resources 
      (mapv (fn [res] (if (= (:owner res) :temp) 
                        (assoc res :owner src)
                        res)) (:resources @app-data) )))
                                             
(defn render-temp-resources [src]
  [:div {:class (str "temp-zone" (if (= 0 (->> @app-data :resources  (filter #(= (:owner %) :temp)) count)) " temp-zone-hidden"))}
      [:p.facility-set "Temporary Zone - Take Resource"]
      [:div.resource-pool
         (doall (map #(render-resource % :temp src) (->> @app-data :resources (filter #(= (:owner %) :temp)) (map :type) distinct)))]
      [:div
         [:button {:on-click #(return-temp-resources 0)}
            "Take"]
         [:button {:on-click #(return-temp-resources src)}
            "Cancel"]]])
   
(defn render-player-resources [player-id]
   [:div 
      [:p.facility-set (get-in @app-data [:players player-id :name])]
      [:div.resource-pool
         (doall (map #(render-resource % player-id :temp) 
                     (->> @app-data :resources (filter #(= (:owner %) player-id)) (map :type) distinct)))]
      ])
      
(defn Game []
   [:div ;;data.react-root
      [:div.container-fluid
         [:div.row
            [:div.col-sm-9    ;; Board
               (doall (map #(render-facility-set %) [:1 :2 :3]))   ;; Facilities
               [:div.row (render-bank-resources)]      ;; Resources
               [:div.row (render-temp-resources ':bank)]      ;; Temporary Area 
               [:div.row (render-player-resources 0)]
               [:div#alert (:alert @app-data)]
               ]
            [:div.col-sm-3]]]])

(defn- update-app-data [game]
   (swap! app-data assoc
      :cards   (:cards game)
      :players (:players game)
      :resources (:resources game)
      :alert nil
      ))
  
(defn render-game [game container]
  (update-app-data game)
  ;;(prn @app-data)
  (r/render [Game] container))