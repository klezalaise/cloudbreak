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
        <input type="text" class="form-control" id="credentialWap.parameters.subscriptionId" name="csubscriptionId" ng-model="credentialWap.parameters.subscriptionId" required placeholder="{{msg.credential_wap_form_subscription_id_placeholder}}">
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
	    <input type="file" data-file="wap.certificate"/>
    </div>
    <!-- .col-sm-9 -->

</div>
</div>
<div class="form-group" ng-class="{'has-error':wapCredentialForm.certificatePassword.$dirty && wapCredentialForm.password.$invalid }">
	<label class="col-sm-3 control-label" for ="password">{{msg.credential_wap_form_password_label}}</label>
	<div class="col-sm-9">
		<input type="password" class="form-control" id="credentialWap.parameters.password" name="cpassword" ng-model="credentialWap.parameters.password" required placeholder="{{msg.credential_wap_form_password_placeholder}}">
		<div class="help-block" ng-show="wapCredentialForm.cpassword.$dirty && wapCredentialForm.cpassword.$invalid">
			<i class="fa fa-warning"></i> {{msg.credential_password_invalid}}
		</div>
	</div>

</div>


<!-- .form-group -->
<div class="form-group" ng-class="{ 'has-error': wapCredentialForm.wap_sshPublicKey.$dirty && wapCredentialForm.gcp_sshPublicKey.$invalid }">
    <label class="col-sm-3 control-label" for="wap_sshPublicKey">{{msg.credential_wap_form_ssh_key_label}}</label>

    <div class="col-sm-9">
        <textarea rows="4" placeholder="{{msg.credential_wap_form_ssh_key_placeholder}}" type="text" class="form-control" ng-model="credentialWap.publicKey" name="wap_sshPublicKey" id="wap_sshPublicKey" required></textarea>
        <div class="help-block" ng-show="wapCredentialForm.wap_sshPublicKey.$dirty && wapCredentialForm.wap_sshPublicKey.$invalid">
            <i class="fa fa-warning"></i> {{msg.credential_ssh_key_invalid}}
        </div>
    </div>
</div>


<div class="form-group" ng-class="{ 'has-error': wapCredentialForm.url.$dirty && wapCredentialForm.url.$invalid }">
    <label class="col-sm-3 control-label" for="url">{{msg.credential_wap_form_url_label}}</label>

    <div class="col-sm-9">
        <input type="text" class="form-control" id="credentialWap.parameters.url" name="url" ng-model="credentialWap.parameters.url" required placeholder="{{msg.credential_wap_form_url_placeholder}}">
        <div class="help-block" ng-show="wapCredentialForm.url.$dirty && wapCredentialForm.url.$invalid">
            <i class="fa fa-warning"></i> {{msg.credential_wap_url_invalid}}
        </div>
    </div>
    <!-- .col-sm-9 -->

</div>



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

