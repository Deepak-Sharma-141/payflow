package com.project.payflow.merchant.dto.request;

import com.project.payflow.common.enums.Enviroment;

public record CreateApiKeyRequest(

        Enviroment enviroment
){
}
