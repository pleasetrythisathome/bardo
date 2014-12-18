(ns bardo.examples.om
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [bardo.ease :refer [wrap ease shift clamp]]
            [bardo.interpolate :refer [interpolate into-lazy-seq mix blend chain pipeline]]
            [bardo.transition :refer [transition]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :as async :refer [<! timeout]]

            [adzerk.boot-reload.client :as reload]))

(enable-console-print!)

(defonce app-state {:pos {:x 0
                          :y 0}})

(defn now []
  (.getTime (js/Date.)))

(defn mover
  [{:keys [x y]} owner {:keys [speed]
                        :or {speed 2000}}]
  (reify
    om/IInitState
    (init-state [_]
      {:intrpl (interpolate {:x x :y y} {:x x :y y})
       :start (now)})
    om/IWillReceiveProps
    (will-receive-props [_ {:keys [x y]}]
      (let [{:keys [intrpl start]} (om/get-state owner)
            dt (/ (- (now) start) speed)
            [px py] ((juxt :x :y) (om/get-props owner))
            intrpl (if (< dt 1)
                     (-> intrpl
                         (shift 0 (- 1 dt) dt 1)
                         (mix (interpolate (intrpl dt) {:x x :y y})))
                     (interpolate {:x px :y py}
                                  {:x x :y y}))]
        (om/set-state! owner :intrpl intrpl)
        (om/set-state! owner :start (now))))
    om/IRenderState
    (render-state [_ {:keys [x y intrpl start]}]
      (let [t (/ (- (now) start) speed)]
        (when (< t 1)
          (let [{:keys [x y]} (intrpl t)]
            (go
              (om/set-state! owner :x x)
              (om/set-state! owner :y y)))))
      (html
       [:div
        {:style {:position "fixed"
                 :top y
                 :left x
                 :border "1px solid #ddd"
                 :padding "5px"}}
        [:div
         "Location"]
        [:div
         (str "x: " (int x))]
        [:div
         (str "y: " (int y))]]))))

(defn app-view
  [data owner]
  (reify
    om/IRender
    (render [this]
      (html
       [:div
        {:on-click (fn [e]
                     (om/update! data :pos {:x (.-pageX e)
                                            :y (.-pageY e)}))
         :style {:position "fixed"
                 :width "100%"
                 :height "100%"}}
        "Click anywhere, click repeated in different corners to curve"
        (om/build mover (:pos data))]))))

(defn mount []
  (om/root app-view app-state
           {:target (. js/document (getElementById "root"))}))

(mount)
(reload/connect "ws://localhost:3449" {:on-jsload mount})
