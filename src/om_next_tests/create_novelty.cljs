(ns om-next-tests.create-novelty
  (:require
   [om.next :as om :refer-macros [defui]]
   [sablono.core :as sab :refer-macros [html]]
   [om-next-tests.basic-parser :as b]
   [cljs.test :refer-macros [is testing async]] 
   )
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-om-next defcard-doc dom-node]]))


(defn merge-fn
  [a b]
  (if (map? a)
    (merge-with merge-fn a b)
    b))

(defonce tid (om/tempid))

(defonce r (om/reconciler {:state {:items [{:db/id 1
                                            :item/name "item1"
                                            :item/id 42}
                                           {:db/id tid
                                            :item/name "item2"}]
                                   :foo :bar
                                   }
                           :normalize true
                           :merge-tree merge-fn
                           :id-key :db/id
                           :parser (om/parser {:read b/basic-reader})}))

(defui Item
  static om/Ident
  (ident [this {:keys [db/id]}]
    [:db/id id])
  static om/IQuery
  (query [this]
    [:db/id :item/id :item/name])
  Object
  (render [this]
    (let [props (om/props this)]
      (html [:div
           [:h1 "id: " (str (:db/id props))]
           [:h2 "item/id: " (:item/id props)]
           [:h2 "item/name: " (:item/name props)]]))))

(def item (om/factory Item {:keyfn :db/id}))

(defui App
  static om/IQuery
  (query [this]
    [{:items (om/get-query Item)}])
  Object
  (render [this]
    (html [:div
           (map item (-> this om/props :items))
           [:button {:on-click (fn [_]
                                 (om/merge! r {'test/blah {:tempids [[:db/id tid] [:db/id 2]]} [:db/id 2] {:item/id 50}})
                                 )} "Merge with stable id"]
           [:button {:on-click (fn [_]
                                 (om/merge! r {'test/blah {:tempids [[:db/id tid] [:db/id 2]]} [:db/id tid] {:item/id 50}})
                                 )} "Merge with tempid id"]
           [:button {:on-click (fn [_]
                                 (om/merge! r {'test/blah {:tempids [[:db/id tid] [:db/id 2]]} [:db/id 2] {:item/id 50} [:db/id tid] {:item/id 50}})
                                 )} "Merge with both"]])))
(defcard state-watch
  (-> r :config :state))

(defcard create-novelty-test
  (dom-node
    (fn [_ node]
      (om/add-root! r App node))))



(comment
 (om/tempid)

 (-> r :config :state deref)

 (om/merge! r {'test/blah {:tempids [[:db/id tid] [:db/id 2]]} [:db/id 2] {:item/id 50}})

 (om/merge! r {[:db/id 1] {:item/name "item1"}})

 (-> (om/ref->components r [:db/id 1]) first om/props)

 (meta (om/get-query Item))

 )


