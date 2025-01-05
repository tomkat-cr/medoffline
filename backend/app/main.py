"""
MedOffline main (FastAPI + Mangum)
"""
from genericsuite.fastapilib.util.create_app import (
    create_app,
    create_handler,
)
# from genericsuite.util.app_logger import log_debug

from app.config.config import Config
from app.routers import ai_assistant as ai_chatbot_endpoint


settings = Config()
app = create_app(app_name=f'{settings.APP_NAME.lower()}-backend',
                 settings=settings)

# Register AI endpoints
app.include_router(ai_chatbot_endpoint.router, prefix='/ai')

handler = create_handler(app)
