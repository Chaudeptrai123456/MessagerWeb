from opentelemetry import trace
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.zipkin.json import ZipkinExporter

from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
from opentelemetry.instrumentation.requests import RequestsInstrumentor


def setup_tracing(app):
    # 1️⃣ Khai báo service name
    resource = Resource.create({
        "service.name": "ai-server"
    })

    # 2️⃣ Tracer provider
    provider = TracerProvider(resource=resource)
    trace.set_tracer_provider(provider)

    # 3️⃣ Zipkin JSON exporter (KHÔNG proto)
    zipkin_exporter = ZipkinExporter(
        endpoint="http://localhost:9411/api/v2/spans"
        # nếu chạy docker → đổi thành http://zipkin:9411/api/v2/spans
    )

    # 4️⃣ Batch processor
    span_processor = BatchSpanProcessor(zipkin_exporter)
    provider.add_span_processor(span_processor)

    # 5️⃣ Auto instrumentation
    FastAPIInstrumentor.instrument_app(app)
    RequestsInstrumentor().instrument()

    print("✅ OpenTelemetry tracing started (ai-server)")
