# infra

Terraform으로 AWS에 kiwobollae를 배포하는 코드입니다.

## 구성

- **network**: VPC, public/private subnet, IGW, NAT
- **security**: ALB / EC2 / RDS 보안그룹
- **ecr**: 백엔드/프론트 이미지 저장소
- **rds**: MySQL (private subnet)
- **alb**: ALB + backend/frontend target group
- **asg**: Launch Template + Auto Scaling Group (EC2가 부팅 시 ECR에서 최신 이미지를 pull해 컨테이너 실행)

## 사전 준비

1. Terraform 상태 저장용 S3 버킷 + DynamoDB 락 테이블을 미리 만들어두고 `environments/prod/backend.tf`에 값을 채워주세요.
2. `environments/prod/terraform.tfvars`를 `terraform.tfvars.example`을 참고해 작성하세요 (DB 비밀번호, JWT secret 등 민감값은 여기 두지 말고 `TF_VAR_*` 환경변수나 AWS Secrets Manager 사용을 권장합니다).

## 실행

```bash
cd infra/environments/prod
terraform init
terraform plan
terraform apply
```

## 무중단 배포 흐름

1. GitHub Actions가 main 브랜치 push 시 백엔드/프론트 이미지를 빌드해 ECR에 push
2. `aws autoscaling start-instance-refresh`로 ASG의 EC2를 한 대씩 새 이미지로 교체
3. ALB 헬스체크를 통과한 인스턴스만 트래픽을 받고, 기존 인스턴스는 커넥션 드레이닝 후 종료 → 무중단
