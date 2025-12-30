'use strict';
const dotenv = require('dotenv');
const envFile = process.env.NODE_ENV === 'docker' ? '.env.docker' : '.env';
dotenv.config({ path: envFile });
const { NodeSDK } = require('@opentelemetry/sdk-node');
const { ZipkinExporter } = require('@opentelemetry/exporter-zipkin');
const { getNodeAutoInstrumentations } = require('@opentelemetry/auto-instrumentations-node');
getNodeAutoInstrumentations({
  '@opentelemetry/instrumentation-redis': {
    enabled: true,
  },
  '@opentelemetry/instrumentation-dns': {
    enabled: false,
  },
  '@opentelemetry/instrumentation-net': {
    enabled: false,
  },
});
const sdk = new NodeSDK({
  traceExporter: new ZipkinExporter({
    url: process.env.ZIP_KIN_URL + '/api/v2/spans', // docker là zip-kin
    serviceName: 'node-service',
  }),
  instrumentations: [getNodeAutoInstrumentations()],
});

sdk.start();
