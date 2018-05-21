#!/bin/bash

export GITHUB_TOKEN=$1
targetBranch=$2
sourceBranch=$3
sourceCommit=$4
GIT_URL=$5

hub_version=2.3.0-pre10
kubectl_version=1.9.6

function die { msg=$1
    echo $1
    exit 1
}

function getHub {
    git="../bin/hub"
    if [[ ! -x ${git} ]]; then
        curl -L https://github.com/github/hub/releases/download/v${hub_version}/hub-linux-amd64-${hub_version}.tgz \
        | tar -xz --strip-components=2 hub-linux-amd64-${hub_version}/bin/hub
    fi
}


function getKubectl {
    export  KUBECONFIG=../bin/kubeconfig.yaml
    #     --kubeconfig=../kubeconfig.yaml
    kubectl="../bin/kubectl"

    if [[ ! -x ../bin/kubectl ]]; then
        curl -L https://storage.googleapis.com/kubernetes-release/release/v${kubectl_version}/bin/linux/amd64/kubectl --output ${kubectl}
        chmod a+x ${kubectl}

        echo "Configure kubectl access from service account"
        CLUSTER=sac
        SA_PATH=/var/run/secrets/kubernetes.io/serviceaccount
        SA_TOKEN=`cat ${SA_PATH}/token`
        ${kubectl} config set-cluster ${CLUSTER} --server=https://kubernetes.default --certificate-authority=${SA_PATH}/ca.crt
        ${kubectl} config set-context ${CLUSTER} --cluster=${CLUSTER}
        ${kubectl} config set-credentials user --token=${SA_TOKEN}
        ${kubectl} config set-context ${CLUSTER} --user=user
        ${kubectl} config use-context ${CLUSTER}
    fi
}


function generateConfigmaps {
    src_dir=../..
    dst_dir=.
    for src_ns_dir in $src_dir/*/; do
        ns=$(basename $src_ns_dir)
        dst_ns_dir=${dst_dir}/${ns}
        if [[ -d $src_ns_dir/configmaps && -d $dst_ns_dir/configmaps ]]; then
            while IFS= read -r src_cfgmap_dir; do
                echo "Configmap dir found $src_cfgmap_dir"
                cfgmap_name=$(basename $src_cfgmap_dir)
                while IFS= read -r _d; do
                    "  Merge .d files from $_d"
                    _d_name=$(basename $_d)
                    _d_name=${_d_name:0:-2}
                    cat $_d/* > $src_cfgmap_dir/${_d_name}
                done < <(find $src_cfgmap_dir -maxdepth 1 -mindepth 1 -type d -name "*.d")
                cfgmap_file=$dst_ns_dir/configmaps/${cfgmap_name}.yaml
                ${kubectl} create configmap ${cfgmap_name} --from-file=${src_cfgmap_dir} --namespace=${ns} --dry-run -o yaml \
                    | grep -v "^  creationTimestamp: null" \
                    > ${cfgmap_file}
                changes=`${git} diff --name-only ${cfgmap_file}`
                echo "Dst configmap: ${cfgmap_file}, changes: ${changes}"
                if [[ -n $changes ]]; then
                    ${git} add ${cfgmap_file}
                    files_updated=$((files_updated+1))
                fi
            done < <(find $src_ns_dir/configmaps -maxdepth 1 -mindepth 1 -type d)
        fi
    done
}


set -x
mkdir -p tmp/bin
cd tmp/bin
getHub
getKubectl
cd ..

if [[ ! -d test-jenkins-repo-dst ]]; then
    bin/hub clone git@github.com:mpashka/test-jenkins-repo-dst.git
fi

cd test-jenkins-repo-dst

${git} fetch

${git} rev-parse --quiet --verify origin/${targetBranch} || die "Target branch ${targetBranch} doesn't exist in dest project"
${git} rev-parse --quiet --verify origin/${sourceBranch}
sourceBranchExists=$?
if [[ $sourceBranchExists == 0 ]]; then
    echo "Source branch ${sourceBranch} exist"
    ${git} checkout ${targetBranch}
    ${git} pull
else
    echo "Source branch ${sourceBranch} doesn't exist in dst repository. Creating..."
    ${git} checkout ${sourceBranch}
    ${git} pull origin ${sourceBranch}
fi

files_updated=0
generateConfigmaps
echo "$files_updated configmaps updated"

# Take all modified files into account including file from unfinished previous step
if [[ `${git} status | grep modified:|wc -l` > 0 ]]; then

    if [[ $sourceBranchExists == 0 ]]; then
        echo "Push changes"
        ${git} commit -m "Commit from original src/${sourceBranch} / ${sourceCommit}"
        ${git} push
    else
        echo "Creating PR for dst: ${sourceBranch} -> ${targetBranch}"
        ${git} checkout -b ${sourceBranch}
        ${git} push --set-upstream origin ${sourceBranch}
        ${git} branch --set-upstream-to origin/${sourceBranch}
        ${git} pull-request -b ${targetBranch} -m "Creating PR from src ${sourceBranch}"
    fi

fi
