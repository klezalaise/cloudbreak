<div class="form-group" ng-class="{ 'has-error': wapCredentialForm.wapname.$dirty && wapCrendentialForm.wapname.$invalid }">
    <label class="col-sm-3 control-label" for="wapname">{{msg.name_label}}</label>

    <div class="col-sm-9">
        <input type="text" ng-pattern="/^[a-z][-a-z0-9]*[a-z0-9]$/" class="form-control" ng-model="credentialWap.name" id="wapname" name="wapname" ng-minlength="5" ng-maxlength="100" required placeholder="{{msg.name_placeholder}}">

        <div class="help-block" ng-show="wapCredentialForm.wapname.$dirty && wapCredentialForm.wapname.$invalid">
            <i class="fa fa-warning"></i> {{msg.credential_name_invalid}}
        </div>
    </div>
    <!-- .col-sm-9 -->

</div>

<div class="form-group" ng-class="{ 'has-error': wapCredentialForm.wapdescription.$dirty && wapCredentialForm.wapdescription.$invalid }">
    <label class="col-sm-3 control-label" for="wapdescription">{{msg.description_label}}</label>

    <div class="col-sm-9">
        <input type="text" class="form-control" ng-model="credentialWap.description" id="wapdescription" name="wapdescription" ng-maxlength="1000" placeholder="{{msg.credential_form_description_placeholder}}">
        <div class="help-block" ng-show="wapCredentialForm.wapdescription.$dirty && wapCredentialForm.wapdescription.$invalid">
            <i class="fa fa-warning"></i> {{msg.credential_description_invalid}}
        </div>
    </div>
    <!-- .col-sm-9 -->

</div>


<!-- .form-group -->
<div class="form-group" ng-class="{ 'has-error': wapCredentialForm.csubscriptionId.$dirty && wapCredentialForm.csubscriptionId.$invalid }">
    <label class="col-sm-3 control-label" for="csubscriptionId">{{msg.credential_wap_form_subscription_id_label}}</label>

    <div class="col-sm-9">
        <input type="text" class="form-control" id="credentialAzureRm.parameters.subscriptionId" name="csubscriptionId" ng-model="credentialWap.parameters.subscriptionId" required placeholder="{{msg.credential_wap_form_subscription_id_placeholder}}">
        <div class="help-block" ng-show="wapCredentialForm.csubscriptionId.$dirty && wapCredentialForm.csubscriptionId.$invalid">
            <i class="fa fa-warning"></i> {{msg.credential_subscription_invalid}}
        </div>
    </div>
    <!-- .col-sm-9 -->

</div>
<!-- .form-group -->
<div class="form-group" ng-class="{ 'has-error': wapCredentialForm.certificate.$dirty && wapCredentialForm.certificate.$invalid }">
    <label class="col-sm-3 control-label" for="certificate">{{msg.credential_wap_form_certificate_label}}</label>

    <div class="col-sm-9">
        <input type="text" class="form-control" id="credentialWap.parameters.tenantId" name="certificate" ng-model="credentialWap.parameters.certificate" required placeholder="{{msg.credential_wap_form_certificate_placeholder}}">
        <div class="help-block" ng-show="wapCredentialForm.certificate.$dirty && wapCredentialForm.certificate.$invalid">
            <i class="fa fa-warning"></i> {{msg.credential_certificate_invalid}}
        </div>
    </div>
    <!-- .col-sm-9 -->

</div>


<!-- .form-group -->



<div class="form-group">
    <label class="col-sm-3 control-label" for="credPublic">{{msg.public_in_account_label}}</label>
    <div class="col-sm-9">
        <input type="checkbox" name="wapCred_publicInAccount" id="wapCred_publicInAccount" ng-model="credentialWap.public">
    </div>
    <!-- .col-sm-9 -->
</div>

<div class="row btn-row">
    <div class="col-sm-9 col-sm-offset-3">
        <a id="wapCredential" ng-disabled="wapCredentialForm.$invalid" ng-click="createWapCredential()" class="btn btn-success btn-block" role="button"><i class="fa fa-plus fa-fw"></i>
            {{msg.credential_form_create}}</a>
    </div>
</div>

