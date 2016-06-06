(ns om-next-tests.recursive-queries
  (:require
   [om.next :as om :refer-macros [defui]]
   [sablono.core :as sab :refer-macros [html]]
   [om-next-tests.basic-parser :as b]
   [cljs.test :refer-macros [is testing async]] 
   )
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest defcard-om-next defcard-doc dom-node]]))


(def tree-data {:db/id 1
                :node/name "root"
                :node/left {:db/id 2
                            :node/name "left-node"
                            :node/left {:db/id 4
                                        :node/name "left-of-left"}
                            :node/right {:db/id 5
                                        :node/name "right-of-left"}
                            }
                :node/right {:db/id 3
                             :node/name "right-node"
                             :node/left {:db/id 6
                                         :node/name "left-of-right"}
                             :node/right {:db/id 7
                                          :node/name "right-of-right"} }})

(defonce r (om/reconciler {:state {:tree tree-data}
                           :normalize true
                           :id-key :db/id
                           :parser (om/parser {:read b/basic-reader})}))

(declare node)

(defui Node
  static om/Ident
  (ident [this {:keys [db/id]}]
    [:db/id id])
  static om/IQuery
  (query [this]
    [:node/name {:node/left 1} {:node/right 1}])
  Object
  (render [this]
    (let [props (om/props this)]
      (html [:div
             [:div.row
              [:div.col-md-12 [:h4.text-center (:node/name props)]]]
             [:div.row
              [:div.col-md-6
               (if-let [l (:node/left props)]
                 (node l))]
              [:div.col-md-6
               (if-let [r (:node/right props)]
                 (node r))]] ]))))

(def node (om/factory Node {:keyfn :db/id}))

(defui App
  static om/IQueryParams
  (params [this]
    {:p 42})
  static om/IQuery
  (query [this]
    [(list {:tree (om/get-query Node)} {:p '?p})])
  Object
  (render [this]
    (let [props (om/props this)]
      (html [:div.container-fluid
             (node (:tree props))
             [:button.btn.btn-default
              {:on-click (fn [_]
                           (om/set-query! this {:params {:p 43}}))} "Click"]]))))

(defcard state-watch
  (-> r :config :state))

(defcard recursive-query-test
  (dom-node
    (fn [_ node]
      (om/add-root! r App node))))
