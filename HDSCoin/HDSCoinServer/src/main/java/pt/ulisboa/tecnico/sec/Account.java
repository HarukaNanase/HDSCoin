package pt.ulisboa.tecnico.sec;

import java.security.PublicKey;

public class Account {
        private PublicKey publicKey;
        private int balance;
        private Transaction[] backlog;

        public Account(PublicKey pkey){
            this.publicKey = pkey;
            balance = 50;
        }


}
