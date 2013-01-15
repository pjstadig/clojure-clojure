;;;; Copyright © 2013 Paul Stadig.  All rights reserved.
;;;;
;;;; The use and distribution terms for this software are covered by the Eclipse
;;;; Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html) which can be
;;;; found in the file epl-v10.html at the root of this distribution.  By using
;;;; this software in any fashion, you are agreeing to be bound by the terms of
;;;; this license.
;;;;
;;;; You must not remove this notice, or any other, from this software.
(ns name.stadig.clojure-clojure.protocols
  (:refer-clojure :only [defprotocol]))

(defprotocol IMeta
  (meta [this]))

(defprotocol IObj
  (with-meta [this meta]))

(defprotocol IConsable
  (cons [this o]))

(defprotocol IEquivable
  (equiv [this o]))

(defprotocol IPersistentCollection
  (empty [this]))

(defprotocol Seqable
  (seq [this]))

(defprotocol ISeq
  (first [this])
  (more [this])
  (next [this])
  (seq? [this]))

(defprotocol Sequential
  (sequential? [this]))

(defprotocol IHashEq
  (hasheq [this]))

(defprotocol IPersistentList
  (list? [this]))

(defprotocol IPersistentStack
  (peek [this])
  (pop [this]))

(defprotocol IReduce
  (reduce [this f] [this f init]))

(defprotocol Counted
  (count [this]))
