var $ = jQuery.noConflict(); 
var formSubmitted = 'false';

jQuery(document).ready(function($) {	

	$('#formSuccessMessageWrap').hide(0);
	$('.formValidationError').fadeOut(0);
	
	// fields focus function starts
	$('input[type="text"], input[type="password"], textarea').focus(function(){
		if($(this).val() == $(this).attr('data-dummy')){
			$(this).val('');
		};	
	});
	// fields focus function ends
		
	// fields blur function starts
	$('input, textarea').blur(function(){
    	if($(this).val() == ''){		    
			$(this).val($(this).attr('data-dummy'));				
		};			
	});
	// fields blur function ends
		
	// submit form data starts	   
    function submitData(currentForm){ 
		console.log('submit form');
		
		formSubmitted = 'true';		
		var formInput = $('#' + currentForm).serialize();
		if(currentForm == "sign-upForm"){
		console.log('inside signup form');
		
		$.ajax({
				type:"get",
				data:formInput,
				dataType: "text",
				crossDomain: true,
				url: myurl+"register/mob",
					success:function(data){
					console.log("data:"+data);
					if(data == "true"){
						window.location.href="login.html";
					} else {
						alert('registration not sucessful !.. try again..');
					}
				}
			});
		
		} else{
			
				
		$.post($('#' + currentForm).attr('action'),formInput, function(data){			
			$('#' + currentForm).hide();
			$('#formSuccessMessageWrap').fadeIn(500);			
		});
		}
	};
	// submit form data function starts	
	// validate form function starts
	function validateForm(currentForm){		
		// hide any error messages starts
	    $('.formValidationError').hide();
		$('.fieldHasError').removeClass('fieldHasError');
	    // hide any error messages ends	
		console.log(currentForm);
		
		$('#' + currentForm + ' .requiredField').each(function( i ){	
		console.log("inside validateForm");
		console.log("formSubmitted"+formSubmitted);
		
			if($(this).val() == '' || $(this).val() == $(this).attr('data-dummy')){				
				$(this).val($(this).attr('data-dummy'));	
				$(this).focus();
				$(this).addClass('fieldHasError');
				$('#' + $(this).attr('id') + 'Error').fadeIn(300);
				return false;					   
			};			
			if($(this).hasClass('requiredEmailField')){				  
				var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
				var tempField = '#' + $(this).attr('id');				
				if(!emailReg.test($(tempField).val())) {
					$(tempField).focus();
					$(tempField).addClass('fieldHasError');
					$(tempField + 'Error2').fadeIn(300);
					return false;
				};			
			};
			/*Validate Phone*/
			if( $( this ).hasClass( 'requiredPhoneField' ) ) {		  
				var phoneReg = /^\d{10}$/;
				tempField = '#' + $( this ).attr('id');				
				if( !phoneReg.test($( tempField ).val()) ) {
					$(tempField).focus();
					$(tempField).addClass('fieldHasError');
					$(tempField + 'Error2').fadeIn(300);
					return false;
				}			
			};
			/*Validate Password
			1)Must Contain atleast 1 sp. char, 1 number ,1 Ucaps,1 lwcaps
			2) it should be atleast 7 digit long.*/
			if( $( this ).hasClass( 'requiredPassword' ) ) {	
//			var passwordReg = (/^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,20}$/);
	var passwordReg = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{7,16}$/;
//				var passwordReg =  /([0-9a-zA-Z\s\r\n@!#\$\^%&*()+=\-\[\]\\\';,\.\/\{\}\|\":<>\?]{8,})$/;
				tempField = '#' + $( this ).attr('id');				
				if( !passwordReg.test($( tempField ).val()) ) {
					$(tempField).focus();
					$(tempField).addClass('fieldHasError');
					$(tempField + 'Error2').fadeIn(300);
					return false;
				}
			};
			if( $( this ).hasClass( 'requiredCPassword' ) ) {
				tempField = '#' + $( this ).attr('id');	
				if( $( tempField ).val() != $( "#password" ).val() ) {
					$(tempField).focus();
					$(tempField).addClass('fieldHasError');
					$(tempField + 'Error3').fadeIn(300);
					return false;
				}
			};
			console.log('near formSubmitted'+formSubmitted);
	
			if(formSubmitted == 'false' && i == $('#' + currentForm + ' .requiredField').length - 1){
			 	console.log('inside formSubmitted');
			 	
				
				submitData(currentForm);
			};			  
   		});		
	};
	// validate form function ends	
	
	// contact button function starts
	$('#sbtBtn').click(function() {	
	console.log('click btn');
		//validateForm($(this).attr('data-formId'));
			submitData($(this).attr('data-formId'));
	    return false;		
	});
	// contact button function ends
	
	
	
});
/*////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*/
/*//////////////////// Document Ready Function Ends                                                                       */
/*////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*/
