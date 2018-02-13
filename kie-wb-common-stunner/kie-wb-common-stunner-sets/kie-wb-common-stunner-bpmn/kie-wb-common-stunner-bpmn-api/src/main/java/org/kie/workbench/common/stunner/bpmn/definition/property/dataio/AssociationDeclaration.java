/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.bpmn.definition.property.dataio;

public interface AssociationDeclaration {

    public static AssociationDeclaration fromString(String encoded) {
        return AssociationParser.parse(encoded);
    }

    public AssociationDeclaration.Pair getPair();

    public boolean isInput();

    interface Pair {

    }

    class FromTo implements AssociationDeclaration.Pair {

        private final String from;
        private final String to;

        public FromTo(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        @Override
        public String toString() {
            return from + "=" + to;
        }
    }

    class SourceTarget implements AssociationDeclaration.Pair {

        private final String source;
        private final String target;

        public SourceTarget(String source, String target) {
            this.source = source;
            this.target = target;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return source + "->" + target;
        }
    }

    class Input implements AssociationDeclaration {

        protected static final String BEGIN_MARK = "[din]";
        private AssociationDeclaration.Pair pair;

        public Input(AssociationDeclaration.Pair pair) {
            this.pair = pair;
        }

        public AssociationDeclaration.Pair getPair() {
            return pair;
        }

        public boolean isInput() {
            return true;
        }

        @Override
        public String toString() {
            return "[din]" + pair.toString();
        }
    }

    class Output implements AssociationDeclaration {

        protected static final String BEGIN_MARK = "[dout]";
        private final AssociationDeclaration.Pair pair;

        public Output(AssociationDeclaration.Pair pair) {
            this.pair = pair;
        }

        public AssociationDeclaration.Pair getPair() {
            return pair;
        }

        public boolean isInput() {
            return false;
        }

        @Override
        public String toString() {
            return "[dout]" + pair.toString();
        }
    }
}

class AssociationParser {

    public static AssociationDeclaration parse(String encoded) {
        if (encoded.startsWith(AssociationDeclaration.Input.BEGIN_MARK)) {
            String rest = encoded.substring(AssociationDeclaration.Input.BEGIN_MARK.length());
            AssociationDeclaration.Pair associationDeclaration = parseAssociation(rest);
            return new AssociationDeclaration.Input(associationDeclaration);
        }

        if (encoded.startsWith(AssociationDeclaration.Output.BEGIN_MARK)) {
            String rest = encoded.substring(AssociationDeclaration.Output.BEGIN_MARK.length());
            AssociationDeclaration.Pair associationDeclaration = parseAssociation(rest);
            return new AssociationDeclaration.Output(associationDeclaration);
        }

        throw new IllegalArgumentException("Cannot parse " + encoded);
    }

    static AssociationDeclaration.Pair parseAssociation(String rest) {
        if (rest.contains("=")) {
            String[] association = rest.split("=");
            return new AssociationDeclaration.FromTo(association[0], association[1]);
        } else {
            String[] association = rest.split("->");
            return new AssociationDeclaration.SourceTarget(association[0], association[1]);
        }
    }
}
