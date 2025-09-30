#!/bin/sh

# Function that permits to extract input parameters in "key=value" format
validate_parameters() {
  parsed_inputs=""
  while [ $# -gt 0 ]; do
    key="$1"
    case "$key" in
      --validate_arazzo)
        parsed_inputs="$parsed_inputs validate_arazzo=$2"
        shift 2
        ;;
      --file_path)
        parsed_inputs="$parsed_inputs file_path=$2"
        shift 2
        ;;
      --log_level)
        parsed_inputs="$parsed_inputs log_level=$2"
        shift 2
        ;;
      --host)
        parsed_inputs="$parsed_inputs host=$2"
        shift 2
        ;;
      --api_key)
        parsed_inputs="$parsed_inputs api_key=$2"
        shift 2
        ;;
      --workflow_id)
        parsed_inputs="$parsed_inputs workflow_id=$2"
        shift 2
        ;;
      --input_file)
        parsed_inputs="$parsed_inputs input_file=$2"
        shift 2
        ;;
      *)
        #echo "Skipping unknown parameter [$key]. Accepted: [validate_arazzo, file_path, log_level, host, api_key, workflow_id, input_file]"
        shift 2
        ;;
    esac
  done

  echo "$parsed_inputs"
}

# Function that permits to retrieve the content of a file
read_file() {
    file="$1"
    if [ ! -f "$file" ]; then
        echo "File not found: $file" >&2
        return 1
    fi
    cat "$file"
}

# Function that permits to retrieve the value from a "key=value" object
# Extract value from "key=value" list, return default if not found
get_value() {
  parsed="$1"
  target_key="$2"
  default_value="$3"
  separator="${4:- }"
  found=""

  # Split using separator, preserving spaces inside values
  IFS="$separator"
  for kv in $parsed; do
    kv=$(printf '%s' "$kv" | xargs)   # trim leading/trailing spaces
    key=${kv%%=*}
    value=${kv#*=}
    if [ "$key" = "$target_key" ]; then
      echo "$value"
      found=1
      return 0
    fi
  done
  IFS=" "  # reset IFS

  # Return default if not found
  if [ -z "$found" ]; then
    echo "$default_value"
  fi
}

# Function that permits to generate the inputs to Arazzo file in JSON form
generate_inputs() {
  inputs_filename="$@"

  # Reading input environment variable's file content
  inputs_content=$(read_file "$inputs_filename")

  sender_type=$(get_value "$inputs_content" "sender.type" "ND" ";")
  sender_id=$(get_value "$inputs_content" "sender.id" "ND" ";")
  sender_psp_id=$(get_value "$inputs_content" "sender.pspId" "ND" ";")
  sender_psp_name=$(get_value "$inputs_content" "sender.pspName" "ND" ";")
  sender_psp_broker_id=$(get_value "$inputs_content" "sender.pspBrokerId" "ND" ";")
  sender_channel_id=$(get_value "$inputs_content" "sender.channelId" "ND" ";")
  receiver_id=$(get_value "$inputs_content" "receiver.id" "ND" ";")
  receiver_organization_id=$(get_value "$inputs_content" "receiver.organizationId" "ND" ";")
  receiver_organization_name=$(get_value "$inputs_content" "receiver.organizationName" "ND" ";")
  total_payments=$(get_value "$inputs_content" "totalPayments" "1" ";")
  sum_payments=$(get_value "$inputs_content" "sumPayments" "1.00" ";")

  current_timestamp=$(date +%s)
  now=$(date -u +"%Y-%m-%d")
  now_date=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  yesterday_date=$( (date --version >/dev/null 2>&1 && date -d "yesterday" +"%Y-%m-%dT%H:%M:%SZ") || date -v-1d +"%Y-%m-%dT%H:%M:%SZ")
  flow_name="${now}${sender_psp_id}-${current_timestamp}"

  single_payment_amount=$(echo "scale=2; $sum_payments / $total_payments" | bc)
  iuv_size=15
  json_content_payments="["
  for i in $(seq 1 $total_payments); do
    idx=$i
    iuv=$(printf "%0${iuv_size}d" $idx)
    json_content_payments="${json_content_payments}{
      \"index\":\"${idx}\",
      \"iuv\": \"${iuv}\",
      \"iur\": \"0000${iuv}\",
      \"idTransfer\": 1,
      \"pay\": ${single_payment_amount},
      \"payStatus\": \"EXECUTED\",
      \"payDate\": \"${yesterday_date}\"
    }"
    # add comma if not last element
    if [ $i -lt $total_payments ]; then
      json_content_payments="${json_content_payments},"
    fi
  done
  json_content_payments="${json_content_payments}]"

  json_content="{
    \"apiKey\": \"$apikey\",
    \"fdr\": \"$flow_name\",
    \"fdrDate\": \"$now_date\",
    \"senderId\": \"$sender_id\",
    \"senderType\": \"$sender_type\",
    \"pspId\": \"$sender_psp_id\",
    \"pspName\": \"$sender_psp_name\",
    \"pspBrokerId\": \"$sender_psp_broker_id\",
    \"channelId\": \"$sender_channel_id\",
    \"receiverId\": \"$receiver_id\",
    \"organizationId\": \"$receiver_organization_id\",
    \"organizationName\": \"$receiver_organization_name\",
    \"regulation\": \"000${current_timestamp}IT\",
    \"regulationDate\": \"$now\",
    \"totPayments\": \"$total_payments\",
    \"sumPayments\": \"$sum_payments\",
    \"payments\": $json_content_payments
  }"
  echo "$json_content"
}

# Function that permits to print the defined parameters
print_parameters() {
  printf "\n%-25s | %-55s\n" "Parameter name" "Value"
  printf "%-25s-+-%-55s\n" "-------------------------" "------------------------------------------------------"
  for token in "$@"; do
    key=$(printf "%s" "$token" | cut -d'=' -f1)
    value=$(printf "%s" "$token" | cut -d'=' -f2-)
    if [[ "$key" == *api_key* ]]; then
      len=${#value}
      if [ "$len" -gt 6 ]; then
        value="${value:0:3}$(printf '%*s' $((len-6)) '' | tr ' ' '*')${value: -3}"
      else
        value="***"
      fi
    fi
    printf "%-25s | %-55s\n" "$key" "$value"
  done
  printf "\n"
}

# Function that permits to print the list of file's workflows
print_workflow_list() {
  printf "\n=== %s\n\n" "Workflows in Arazzo file ==="
  echo "$@" | awk '
    BEGIN {
      # Print table header
      printf "%-45s | %-50s\n", "Name", "Description"
      printf "----------------------------------------------+----------------------------------------------------\n"
    }
    # Match lines that start with "- "
    /^- / {
      # Remove leading "- "
      sub(/^- /, "", $0)
      # Split on ":" into name and description
      n = index($0, ":")
      name = substr($0, 1, n-1)
      desc = substr($0, n+1)
      gsub(/^ +| +$/, "", name)   # trim spaces
      gsub(/^ +| +$/, "", desc)
      printf "%-45s | %-50s\n", name, desc
    }
  '
}

# Function that permits to print the detail of chosen workflow
print_workflow_detail() {
  workflow_text="$@"

  # Print workflow header
  workflow_name=$(echo "$workflow_text" | awk -F': ' '/^Workflow:/ {print $2}')
  workflow_summary=$(echo "$workflow_text" | awk -F': ' '/^Summary:/ {print $2}')

  printf "\n\n=== %s\n\n" "Details for chosen workflow ==="
  printf "%-20s : %s\n" "Workflow" "$workflow_name"
  printf "%-20s : %s\n" "Summary" "$workflow_summary"

  # Inputs section
  printf "\n\n=== %s\n\n" "Workflow inputs ==="
  printf "%-25s | %-15s | %-70s\n" "Field Name" "Type" "Description"
  printf "%-25s-+-%-15s-+-%-70s\n" "------------------------" "---------------" "----------------------------------------------------------------------"
  echo "$workflow_text" | awk '
    BEGIN { in_inputs=0 }
    /^Inputs:/ { in_inputs=1; next }
    /^Steps:/ { in_inputs=0; in_steps=1; next }
    in_inputs && /^- / {
      line=$0
      sub(/^- /,"",line)
      n=index(line,":")
      left=substr(line,1,n-1)
      desc=substr(line,n+1)
      gsub(/^[ \t]+|[ \t]+$/,"",left)
      gsub(/^[ \t]+|[ \t]+$/,"",desc)

      # Extract type inside parentheses
      type=""
      if (match(left, /\([^\)]*\)/)) {
        type=substr(left, RSTART+1, RLENGTH-2)
        sub(/\([^\)]*\)/,"",left)
      }
      key=left
      printf "%-25s | %-15s | %-70s\n", key, type, desc
    }
  '

  # Steps section
  printf "\n\n=== %s\n" "Workflow steps ==="
  printf "\n%-35s | %-50s\n" "Step Name" "Description"
  printf "%-35s-+-%-50s\n" "-----------------------------------" "--------------------------------------------------"
  echo "$workflow_text" | awk '
    BEGIN { in_steps=0 }
    /^Steps:/ { in_steps=1; next }
    in_steps && /^[0-9]+\./ {
      line=$0
      n=index(line,":")
      step=substr(line,1,n-1)
      desc=substr(line,n+1)
      gsub(/^[ \t]+|[ \t]+$/,"",step)
      gsub(/^[ \t]+|[ \t]+$/,"",desc)
      # Remove numeric prefix from step
      sub(/^[0-9]+\.\s*/,"",step)
      printf "%-35s | %-50s\n", step, desc
    }
  '
  printf "\n"
}

# Function that permits to print the field of chosen workflow inputs
print_workflow_input() {
  json="$1"

  # Stamp table header
  printf "\n\n=== %s\n\n" "Workflow input values ==="
  printf "%-25s | %-55s\n" "Parameter name" "Value"
  printf "%-25s-+-%-55s\n" "-------------------------" "------------------------------------------------------"

  # Loop over JSON keys and print key=value
  echo "$json" | jq -r 'to_entries | .[] | "\(.key)=\(.value)"' | while IFS='=' read -r key value; do
    if [[ "$key" =~ [aA][pP][iI][kK][eE][yY] ]]; then
      len=${#value}
      if [ "$len" -gt 6 ]; then
        value="${value:0:3}$(printf '%*s' $((len-6)) '' | tr ' ' '*')${value: -3}"
      else
        value="***"
      fi
    fi
    printf "%-25s | %-55s\n" "$key" "$value"
  done

  printf "\n"
}


# Function that permits to print the current execution phase
print_step() {
    print_input="$1"
    line_len=50  # total length of the separator line
    input_len=${#print_input}  # length of the input string

    # calculate spaces to prepend so text appears centered
    left_space=$(( (line_len - input_len) / 2 ))
    if [ "$left_space" -lt 0 ]; then
        left_space=0
    fi

    # build a string with left_space number of spaces
    spaces=""
    i=0
    while [ "$i" -lt "$left_space" ]; do
        spaces="$spaces "
        i=$((i+1))
    done

    # print the banner
    printf "\n\n==================================================\n"
    printf '%s\n' "${spaces}${print_input}"
    printf "==================================================\n"
}

print_step_outputs() {
  run_result="$@"
  printf "\n\n=== %s\n\n" "Steps output ==="
  step_outputs=$(echo "$run_result" | sed -n "s/.*step_outputs=\({.*}\), inputs.*/\1/p")
  json=$(echo "$step_outputs" | sed "s/'/\"/g")
  for step in $(echo "$json" | jq -r 'keys[]'); do
    echo "[$step]"
    echo "$json" | jq ".\"$step\""
    echo "\n"
  done
}

# Main function
main() {
  
  # Extracting parsed input including in a list
  parsed_inputs=$(validate_parameters "$@")
  print_step "CONFIGURATION INPUTS"
  print_parameters $parsed_inputs

  # Extracting required parameters for all steps
  log_level=$(get_value "$parsed_inputs" log_level "INFO")
  validate_arazzo=$(get_value "$parsed_inputs" validate_arazzo "false")
  file_path=$(get_value "$parsed_inputs" file_path "./fdr.arazzo.yaml")
  workflow_id=$(get_value "$parsed_inputs" workflow_id "default-workflow")
  host=$(get_value "$parsed_inputs" host "localhost:8080")
  apikey=$(get_value "$parsed_inputs" api_key "Invalid")
  inputs_filename=$(get_value "$parsed_inputs" input_file "./inputs.env")

  # Executing "file validation" step
  print_step "VALIDATING ARAZZO FILE"
  printf "\n=== %s\n\n" "File validation outcome ==="
  if [ "$validate_arazzo" = "true" ]; then
    arazzo-generator validate "$file_path"
  else
    printf "Skipping Arazzo file validation because '--validate_arazzo' parameter isn't [true]\n"
  fi
  printf "\n"
  workflow_list="$(arazzo-runner list-workflows "$file_path")"
  print_workflow_list "$workflow_list"
  printf "\n"

  # Executing "workflow execution" step
  print_step "ANALYZING ARAZZO FILE"
  workflow_detail="$(arazzo-runner describe-workflow --workflow-id "$workflow_id" "$file_path")"
  print_workflow_detail "$workflow_detail"

  # Executing "workflow execution" step
  print_step "EXECUTING WORKFLOW [$workflow_id]"
  workflow_inputs="$(generate_inputs "$inputs_filename")"
  print_workflow_input "$workflow_inputs"
  printf "\n\n=== %s\n\n" "Running workflow traces ==="
  run_result="$(arazzo-runner \
    --log-level "$log_level" \
    execute-workflow ./fdr.arazzo.yaml \
    --workflow-id "$workflow_id" \
    --inputs "$workflow_inputs" \
    --server-variables "{\"host\": \"$host\"}")"
  print_step_outputs "$run_result"
}

# Start execution
main "$@"
