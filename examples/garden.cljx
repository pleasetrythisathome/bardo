(ns garden
  (:require [bardo.interpolate :refer [IFresh IInterpolate -interpolate interpolate]]
            [garden.color :as color :refer [rgb as-color]]))

(extend-protocol IFresh
  garden.color.CSSColor
  (fresh [s]
    (rgb 255 255 255)))

(extend-protocol IInterpolate
  garden.color.CSSColor
  (-interpolate [start end]
    (fn [t]
      (as-color
       (merge-with (fn [a b]
                     (when (and a b)
                       ((-interpolate a b) t)))
                   start end)))))

(map (interpolate (rgb 255 0 0) (rgb 0 255 0)) [0 0.5 1])
;; => (#garden.color.CSSColor{:alpha nil, :lightness nil, :saturation nil, :hue nil, :blue 0, :green 0, :red 255}
;;     #garden.color.CSSColor{:red 127.5, :green 127.5, :blue 0.0, :hue nil, :saturation nil, :lightness nil, :alpha nil}
;;     #garden.color.CSSColor{:alpha nil, :lightness nil, :saturation nil, :hue nil, :blue 0, :green 255, :red 0}
