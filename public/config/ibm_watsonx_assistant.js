window.watsonAssistantChatOptions = {
    integrationID: "{IBM_WATSONX_INTEGRATION_ID}", // The ID of this integration.
    region: "{IBM_WATSONX_REGION}", // The region your integration is hosted in.
    serviceInstanceID: "{IBM_WATSONX_SERVICE_INSTANCE_ID}", // The ID of your service instance.
    onLoad: async (instance) => { await instance.render(); }
};
setTimeout(function(){
    const t=document.createElement('script');
    t.src="https://web-chat.global.assistant.watson.appdomain.cloud/versions/latest/WatsonAssistantChatEntry.js";
    document.head.appendChild(t);
});
