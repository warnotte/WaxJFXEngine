package org.openjfx.hellofx;
  // Exemple d'objet métier
    public class Flotteur {
        private String name;

        public Flotteur(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }