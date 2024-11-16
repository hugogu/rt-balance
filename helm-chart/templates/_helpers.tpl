{{- if .Values.kafka.enabled }}
{{- include "kafka-deployment.yaml" . }}
{{- end }}

{{- if .Values.zookeeper.enabled }}
{{- include "zookeeper-deployment.yaml" . }}
{{- end }}

{{- if .Values.redis.enabled }}
{{- include "redis-deployment.yaml" . }}
{{- end }}

{{- if .Values.ingress.enabled }}
{{- include "ingress-deployment.yaml" . }}
{{- end }}

