{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for service containers
*/}}
{{- define "crime-assessment-service.env-vars" }}
env:
  - name: AWS_REGION
    value: {{ .Values.aws_region }}
  - name: SENTRY_DSN
    valueFrom:
      secretKeyRef:
        name: crime-assessment-service-env-variables
        key: SENTRY_DSN
  - name: SENTRY_ENV
    value: {{ .Values.host_env }}
  - name: SENTRY_SAMPLE_RATE
    value: {{ .Values.sentry.sampleRate | quote }}
  - name: SCOPE_SCHEDULED_TASKS
    value: {{ .Values.scope }}

  - name: DATASOURCE_USERNAME
    valueFrom:
        secretKeyRef:
            name: crime-assessment-service-env-variables
            key: DATASOURCE_USERNAME
  - name: DATASOURCE_PASSWORD
    valueFrom:
        secretKeyRef:
            name: crime-assessment-service-env-variables
            key: DATASOURCE_PASSWORD
  - name: DATASOURCE_HOST_PORT
    valueFrom:
        secretKeyRef:
            name: crime-assessment-service-env-variables
            key: DATASOURCE_HOST_PORT
  - name: DATASOURCE_DBNAME
    valueFrom:
        secretKeyRef:
            name: crime-assessment-service-env-variables
            key: DATASOURCE_DBNAME
{{- end -}}
