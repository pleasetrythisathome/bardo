(ns garden
  (:require [bardo.interpolate :refer [IFresh IInterpolate -interpolate interpolate]]
            [garden.color :as color]))

(extend-protocol IFresh
  garden.color.CSSColor
  (fresh [s]
    (color/rgb 255 255 255)))

(extend-protocol IInterpolate
  garden.color.CSSColor
  (-interpolate [start end]
    (let [[start end] (map color/as-rgb [start end])]
      (fn [t]
        (color/color+ start (color/color* t (color/color- end start)))))))

(-> (interpolate (color/hsl 10 50 50) (color/rgb 0 255 0))
    (map [0 0.25 0.5 0.75 1]))
;; =>
;; ({:alpha nil,
;;   :lightness nil,
;;   :saturation nil,
;;   :hue nil,
;;   :blue 64,
;;   :green 85,
;;   :red 191}
;;  {:red 191.0,
;;   :green 127.5,
;;   :blue 64.0,
;;   :hue nil,
;;   :saturation nil,
;;   :lightness nil,
;;   :alpha nil}
;;  {:red 191.0,
;;   :green 170.0,
;;   :blue 64.0,
;;   :hue nil,
;;   :saturation nil,
;;   :lightness nil,
;;   :alpha nil}
;;  {:red 191.0,
;;   :green 212.5,
;;   :blue 64.0,
;;   :hue nil,
;;   :saturation nil,
;;   :lightness nil,
;;   :alpha nil}
;;  {:alpha nil,
;;   :lightness nil,
;;   :saturation nil,
;;   :hue nil,
;;   :blue 64,
;;   :green 255,
;;   :red 191})
