package com.example.julio.apptaller.Model;

import java.util.List;

/**
 * Created by Julio on 20/09/2018.
 */

public class FCMResponse {
    public long muticast_id;
    public int success;
    public int failure;
    public int cannonical_ids;
    public List<Result> results;

    public FCMResponse(){

    }

    public FCMResponse(long muticast_id, int success, int failure, int cannonical_ids, List<Result> results){
        this.muticast_id = muticast_id;
        this.success = success;
        this.failure = failure;
        this.cannonical_ids = cannonical_ids;
        this.results = results;
    }

    public long getMuticast_id() {
        return muticast_id;
    }

    public void setMuticast_id(long muticast_id) {
        this.muticast_id = muticast_id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCannonical_ids() {
        return cannonical_ids;
    }

    public void setCannonical_ids(int cannonical_ids) {
        this.cannonical_ids = cannonical_ids;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}
