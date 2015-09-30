package com.ekuater.admaker.command.adres;

import com.ekuater.admaker.command.BaseCommand;
import com.ekuater.admaker.datastruct.ConstantCode;
import com.ekuater.admaker.datastruct.pojo.ResVersion;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Leo on 2015/6/17.
 *
 * @author LinYong
 */
public class ResVersionCommand extends BaseCommand {

    public static final String URL = "/services/source/version.json";

    public ResVersionCommand(String type) {
        super();
        setRequestMethod(ConstantCode.REQUEST_GET);
        setUrl(URL);
        putParam("type", type);
    }

    @SuppressWarnings("unused")
    public static class Response extends BaseCommand.Response {

        @SerializedName("sourceVersionArray")
        private ResVersion[] resVersions;

        public Response() {
            super();
        }

        public ResVersion[] getResVersions() {
            return resVersions;
        }
    }
}
