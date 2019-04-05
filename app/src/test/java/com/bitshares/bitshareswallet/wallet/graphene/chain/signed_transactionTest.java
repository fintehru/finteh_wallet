package com.bitshares.bitshareswallet.wallet.graphene.chain;

import com.bitshares.bitshareswallet.wallet.fc.crypto.sha256_object;
import com.bitshares.bitshareswallet.wallet.private_key;

import org.bitcoinj.core.ECKey;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

import de.bitsharesmunich.graphenej.BrainKey;

import static org.junit.Assert.*;

public class signed_transactionTest {

    @Test
    public void sign() {
        int num_tries = 100;

        BrainKey brainKey = new BrainKey("This is a test", 1);
        ECKey ecKey = brainKey.getPrivateKey();
        private_key pK = new private_key(ecKey.getPrivKeyBytes());

        types.private_key_type privateKey = new types.private_key_type(pK);

        sha256_object chain_id = sha256_object.create_from_string("ABC123");

        HashSet<compact_signature> signatures = new HashSet<compact_signature>();
        
        for(int i = 0; i < num_tries; ++i)
        {
            signed_transaction trx = new signed_transaction();
            trx.expiration = new java.util.Date();
            trx.operations = new ArrayList<operations.operation_type>();
            trx.extensions = new HashSet<types.void_t>();
            trx.sign(privateKey, chain_id);
            signatures.add(trx.signatures.get(0));
        }

        assertEquals(num_tries, signatures.size());
    }
}