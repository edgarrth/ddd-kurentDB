# Ejemplos de respuestas

## POST /agilepm/v1/tenants/{tenantId}/products

```json
{
  "tenantId": "tenant-a",
  "productId": "product-001",
  "status": "PRODUCT_INITIATED"
}
```

## POST /releases

```json
{
  "productId": "product-001",
  "status": "RELEASE_SCHEDULED"
}
```

## POST /sprints

```json
{
  "productId": "product-001",
  "status": "SPRINT_PLANNED"
}
```

## GET /agilepm/v1/tenants/{tenantId}/products/{productId}

```json
{
  "tenantId": "tenant-a",
  "productId": "product-001",
  "name": "AgilePM Platform",
  "description": "Product used to manage Scrum planning",
  "productOwnerId": "owner-001",
  "discussionId": "discussion-abc",
  "sprintIds": ["sprint-001"],
  "releaseIds": ["release-2026-q3"],
  "version": 4
}
```
